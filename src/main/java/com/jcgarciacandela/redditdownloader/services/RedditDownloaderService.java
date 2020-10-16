package com.jcgarciacandela.redditdownloader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcgarciacandela.redditdownloader.util.UtilMisc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RedditDownloaderService {
  private final static Logger log = LoggerFactory.getLogger(RedditDownloaderService.class);

  private final ApplicationContext context;

  @Value("#{'${subreddits}'.split(',')}")
  private ArrayList<String> subreddits;

  private final LinkedHashMap<String, String> urlImages = new LinkedHashMap<>();

  @Value("${outputPath}")
  private String basePath;

  @Value("#{new Boolean('${onlyOnePass:false}')}")
  boolean onlyOnePass;


  int limit;
  @Value("${userAgent:'Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5'}")
  String userAgent;

  public RedditDownloaderService(ApplicationContext context) {
    this.context = context;
  }

//  @PostConstruct
//  public void onStartup() {
//    retrievePosts();
//  }

  @Scheduled(fixedRateString = "${imagesDelay}")
  private void retrieveImages() {
    if (urlImages.size() == 0) return;

    String filePath = urlImages.entrySet().iterator().next().getKey();
    String url = urlImages.remove(filePath);

    if (url.contains("imgur.com") && url.contains("gifv")) {
      url = UtilMisc.recuperaRegexpGrupo("(?s)content=\"([^\"]*?\\.mp4)\"", UtilMisc.readURLHTTPS(url,userAgent), 1);
    }

    if (url != null && !url.trim().isEmpty()) {
      new File(filePath.substring(0, filePath.lastIndexOf(System.getProperty("file.separator")))).mkdirs();
      byte[] contenidoBinario = UtilMisc.readBinaryHTTPS(url);
      log.info("[" + urlImages.size() + " images in queue]\tWriting " + filePath);
      UtilMisc.escribeFicheroBinario(filePath, contenidoBinario);
    }
  }

  @Scheduled(fixedRateString = "${subredditsDelay}")
  private void retrievePosts() {
    if (subreddits.size() == 0 && urlImages.size() == 0) {
      ((ConfigurableApplicationContext) context).close();
    }
    if (subreddits.size() == 0) return;
    String subredditActual = subreddits.remove(0);
    if (!onlyOnePass) {
      subreddits.add(subredditActual);
    }

    String urlSubreddit = "https://reddit.com/r/" + subredditActual + ".json?limit=" + limit;
    String content = UtilMisc.readURLHTTPS(urlSubreddit,userAgent);
    log.info("Retrieving posts from " + subredditActual);
    ObjectMapper mapper = new ObjectMapper();

    try {
      Map<String, Object> map = mapper.readValue(content, Map.class);
      if (map != null) {
        map = (Map<String, Object>) map.get("data");
        ArrayList<Map<String, Object>> children = (ArrayList<Map<String, Object>>) map.get("children");

        for (Map<String, Object> item : children) {
          map = (Map<String, Object>) item.get("data");
          String url = (String) map.get("url");
          String author = (String) map.get("author");

          String nombreFichero = url.substring(url.lastIndexOf("/") + 1);
          if (nombreFichero.endsWith("jpg") || nombreFichero.endsWith("gifv")) {
            String outputFilePath = basePath + System.getProperty("file.separator") + subredditActual + System.getProperty("file.separator") + author + "_" + nombreFichero;
            if (url.contains("imgur.com") && url.contains("gifv")) {
              outputFilePath = outputFilePath.replace(".gifv", ".mp4");
            }
            if (!new File(outputFilePath).exists()) {
              urlImages.put(outputFilePath, url);
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }


  }

}

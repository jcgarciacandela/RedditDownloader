package com.jcarlos.redditdownloader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcarlos.redditdownloader.util.UtilMisc;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RedditDownloader {
  @Autowired
  private ApplicationContext context;

  @Value("#{'${subreddits}'.split(',')}")
  private ArrayList<String> subreddits;

  private LinkedHashMap<String, String> urlImages = new LinkedHashMap<>();

  @Value("${ruta}")
  private String rutaBase;

  @Value("#{new Boolean('${onlyOnePass:false}')}")
  boolean onlyOnePass;

  @PostConstruct
  public void onStartup() {
    retrievePosts();
  }

  @Scheduled(fixedRateString = "${retardoImagenes}")
  private void retrieveImages() {

    if (urlImages.size() == 0) return;

    String filePath = urlImages.entrySet().iterator().next().getKey();
    String url = urlImages.remove(filePath);

    if (url.contains("imgur.com") && url.contains("gifv")) {
      filePath = filePath.replace(".gifv", ".mp4");
    }

    if (url.contains("imgur.com") && url.contains("gifv")) {
      url = UtilMisc.recuperaRegexpGrupo("(?s)content=\"([^\"]*?\\.mp4)\"", UtilMisc.leeURLHTTPS(url), 1);
    }
    if (url != null && !url.isBlank()) {
      new File(filePath.substring(0, filePath.lastIndexOf(System.getProperty("file.separator")))).mkdirs();
      byte[] contenidoBinario = UtilMisc.leeURLImagenHTTPS(url);
      System.out.println("Writing " + filePath);
      UtilMisc.escribeFicheroBinario(filePath, contenidoBinario);
    }
  }

  @Scheduled(fixedRateString = "${retardoSubreddits}")
  private void retrievePosts() {

    if (subreddits.size() == 0 && urlImages.size() == 0) {
      ((ConfigurableApplicationContext) context).close();
    }
    if (subreddits.size() == 0) return;
    String subredditActual = subreddits.remove(0);
    if (!onlyOnePass) {
      subreddits.add(subredditActual);
    }

    String urlSubreddit = "https://reddit.com/r/" + subredditActual + ".json?limit=100";
    String contenido = UtilMisc.leeURLHTTPS(urlSubreddit);
    System.out.println("Retrieving posts from " + subredditActual);
    ObjectMapper mapper = new ObjectMapper();

    try {
      Map<String, Object> map = mapper.readValue(contenido, Map.class);
      if (map != null) {
        map = (Map<String, Object>) map.get("data");
        ArrayList<Map<String, Object>> children = (ArrayList<Map<String, Object>>) map.get("children");

        for (Map<String, Object> item : children) {
          map = (Map<String, Object>) item.get("data");
          String url = (String) map.get("url");
          String author = (String) map.get("author");

          String nombreFichero = url.substring(url.lastIndexOf("/") + 1);

          if (nombreFichero.endsWith("jpg") || nombreFichero.endsWith("gifv")) {
            String rutaSalida = rutaBase + System.getProperty("file.separator") + subredditActual + System.getProperty("file.separator") + author + "_" + nombreFichero;
            File testRutaSalida = new File(rutaSalida);
            if (!testRutaSalida.exists()) {
              urlImages.put(rutaSalida, url);
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }


  }

}

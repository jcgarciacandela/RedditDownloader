package com.jcarlos.redditdownloader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcarlos.redditdownloader.RedditdownloaderApplication;
import com.jcarlos.redditdownloader.util.UtilMisc;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.PriorityQueue;

@Service
public class RedditDownloader {
  @Autowired
  private ApplicationContext context;

  @Value("#{'${subreddits}'.split(',')}")
  private ArrayList<String> subreddits;

  @Value("${ruta}")
  private String rutaBase;

  @Value("#{new Boolean('${onlyOnePass:false}')}")
  boolean onlyOnePass;

  @PostConstruct
  public void onStartup() {
    recuperaPosts();
  }

  @Scheduled(fixedRateString = "${retardoSubreddits}", initialDelay = 0)
  private void recuperaPosts() {

    if (subreddits.size() == 0) {
      ((ConfigurableApplicationContext) context).close();
      return;
    }
    String subredditActual = subreddits.remove(0);
    if (!onlyOnePass) {
      subreddits.add(subredditActual);
    }

    String urlSubreddit = "https://reddit.com/r/" + subredditActual + ".json?limit=100";
    String contenido = UtilMisc.leeURLHTTPS(urlSubreddit);
    System.out.println(subredditActual);
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
//            System.out.println(author+"\t"+url);
            String rutaSalida = rutaBase + System.getProperty("file.separator") + subredditActual + System.getProperty("file.separator") + author + "_" + nombreFichero;
//            System.out.println(rutaSalida);

            if (url.contains("imgur.com") && url.contains("gifv")) {
              rutaSalida = rutaSalida.replace(".gifv", ".mp4");
            }

            File testRutaSalida = new File(rutaSalida);
            if (!testRutaSalida.exists()) {
              System.out.println(subredditActual + "\t" + url);
              if (url.contains("imgur.com") && url.contains("gifv")) {
                String contenidoImgur = UtilMisc.leeURLHTTPS(url);
//                System.out.println(contenidoImgur);
                url = UtilMisc.recuperaRegexpGrupo("(?s)content=\"([^\"]*?\\.mp4)\"", contenidoImgur, 1);
              }
              if (url != null && !url.isBlank()) {
                new File(rutaSalida.substring(0, rutaSalida.lastIndexOf(System.getProperty("file.separator")))).mkdirs();
                byte[] contenidoBinario = UtilMisc.leeURLImagenHTTPS(url);
                UtilMisc.escribeFicheroBinario(rutaSalida, contenidoBinario);
                Thread.sleep(2000); //TODO: A properties
              }

            }
          }
        }
      }
//      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }


  }

}

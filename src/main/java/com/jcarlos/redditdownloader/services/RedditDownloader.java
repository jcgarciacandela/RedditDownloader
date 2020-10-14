package com.jcarlos.redditdownloader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcarlos.redditdownloader.util.UtilMisc;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.PriorityQueue;

@Service
public class RedditDownloader {

//  @Value("${subreddits}")
//  private String[] subreddits;

//  @Value("${retardoSubreddits}")
//  private int retardoSubreddits;

  @Value("#{'${subreddits}'.split(',')}")
  private ArrayList<String> subreddits;

  @Value("${ruta}")
  private String rutaBase;

  @Scheduled(fixedRateString = "${retardoSubreddits}", initialDelay = 0)
  private void recuperaPosts() {
    String subredditActual = subreddits.remove(0);
    subreddits.add(subredditActual);

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
              rutaSalida=rutaSalida.replace(".gifv",".mp4");
            }

            File testRutaSalida = new File(rutaSalida);
            if (!testRutaSalida.exists()) {
              System.out.println(subredditActual+"\t"+url);
              if (url.contains("imgur.com")  && url.contains("gifv")) {
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

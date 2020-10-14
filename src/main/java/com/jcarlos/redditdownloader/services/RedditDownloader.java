package com.jcarlos.redditdownloader.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RedditDownloader {

  @Scheduled(fixedRate = 5000)
  private void recuperaPosts() {
    System.out.println("hola");
  }

}

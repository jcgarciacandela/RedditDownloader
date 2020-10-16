package com.jcgarciacandela.redditdownloader.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.net.ssl.*;
import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class UtilMisc {
  private static final Logger log  = LoggerFactory.getLogger(UtilMisc.class);

  public static String readURLHTTPS(String direccion, String userAgent) {

    StringBuilder salida = new StringBuilder();
    try {
      CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
      direccion=direccion.replace("http://","https://");
      URL myUrl = new URL(direccion);
      HttpsURLConnection conn = (HttpsURLConnection) myUrl.openConnection();
      conn.setRequestProperty("User-Agent", userAgent);
      InputStream is = conn.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String inputLine;
      while ((inputLine = br.readLine()) != null) salida.append(inputLine);
      br.close();
    } catch (Exception e) {
      log.error("Error: "+direccion);

      e.printStackTrace();
    }
    return salida.toString();
  }

  public static byte[] readBinaryHTTPS(String direccion) {

    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    // -Dhttps.protocols=TLSv1.1,TLSv1.2
    try {
      TrustManager[] trustAllCerts = new TrustManager[]{
              new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                  return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
              }
      };

      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = (hostname, session) -> true;
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      direccion = direccion.replace(" ", "%20");
      URL url = new URL(direccion);
      InputStream in = new BufferedInputStream(url.openStream());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n = 0;
      while (-1 != (n = in.read(buf))) out.write(buf, 0, n);
      out.close();
      in.close();
      return out.toByteArray();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public static void escribeFicheroBinario(String ruta, byte[] content) {
    try {
      FileOutputStream fos = new FileOutputStream(ruta);
      fos.write(content);
      fos.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static String recuperaRegexpGrupo(String regExp, String content, int group) {
    if (content == null || regExp == null) return "";
    try {
      Pattern regex = Pattern.compile(regExp, Pattern.DOTALL);
      Matcher regexMatcher = regex.matcher(content);
      if (regexMatcher.find()) return regexMatcher.group(group).trim();
    } catch (PatternSyntaxException ex) {
      ex.printStackTrace();
    }
    return "";
  }
}

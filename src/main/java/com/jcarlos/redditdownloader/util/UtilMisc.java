package com.jcarlos.redditdownloader.util;

import org.springframework.beans.factory.annotation.Value;

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



  public static String leeURLHTTPS(String direccion) {
    System.setProperty("http.agent", "java downloader 0.1" );
    String httpsURL = "https://your.https.url.here/";
    StringBuffer salida=new StringBuffer("");
    try {
      CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
      URL myUrl = new URL(direccion);
      HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5");
      InputStream is = conn.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);

      String inputLine;

      while ((inputLine = br.readLine()) != null) {
        salida.append(inputLine);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return salida.toString();
  }

  public static byte[] leeURLImagenHTTPS(String direccion) {
    System.out.println(new java.util.Date() + " Leyendo imagen: " + direccion);
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    // -Dhttps.protocols=TLSv1.1,TLSv1.2
    try {

      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
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
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      direccion = direccion.replace(" ", "%20");
      URL url = new URL(direccion);
      InputStream in = new BufferedInputStream(url.openStream());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n = 0;
      while (-1 != (n = in.read(buf))) {
        out.write(buf, 0, n);
      }
      out.close();
      in.close();
      byte[] response = out.toByteArray();
      return response;
    } catch (Exception ex) {
      System.err.println(ex);
      return null;
    }
  }

  public static void escribeFicheroBinario(String ruta, byte[] contenido) {
    try {
      FileOutputStream fos = new FileOutputStream(ruta);
      fos.write(contenido);
      fos.close();

    } catch (IOException ex) {
      System.err.println(ex);
    }
  }

  public static String recuperaRegexpGrupo(String regExp, String contenido, int grupo) {
    if (contenido == null) {
      contenido = "";
    }
    try {
      Pattern regex = Pattern.compile(regExp, Pattern.DOTALL);
      Matcher regexMatcher = regex.matcher(contenido);
      if (regexMatcher.find()) {
        return regexMatcher.group(grupo).trim();
      }
    } catch (PatternSyntaxException ex) {
      System.err.println(ex);
    }
    return "";
  }
}

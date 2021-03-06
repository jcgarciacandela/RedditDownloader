# RedditDownloader

Retrieve images (and some mp4) from specified subreddits. Uses Spring Boot with @Scheduled methods.

Usage:
Windows: `mvnw.cmd spring-boot:run`
Linux: `mvnw spring-boot:run`
Or generate `mvn clean package` and run with `java -jar`

Edit application.properties before compile/run or put in the same folder as jar (https://stackoverflow.com/questions/39427675/application-properties-outside-jar-file-how-to/39428564)

application.properties
```
#Subreddits to scrape
subreddits=aww,eyebleach,pics

#In millisecons. Delay between subreddits scraping
subredditsDelay=60000

#In millisecons. Delay between images scraping
imagesDelay=3000

#Post to retrieve for each subreddit
postsToRetrieve=100

#Output path for images
outputPath = f:\\descargasReddit

#If true, process subreddits and exit. If false or missing, keeps processing as a circular queue
onlyOnePass=true

userAgent=Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5
```

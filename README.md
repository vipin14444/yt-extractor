# yt-extractor
Steps to Get Youtube CDN

#Step-0:
  Add this library 
  	dependencies {
	        implementation 'com.github.rakeshrajput537:yt-extractor:Tag'
	}

#Step-1:
  Create a object of YoutubeJExtractor

  
 #Step-2:
    Pass the video Id in getUrlForAndroidEmbeddedPlayer or getUrl methods
    It will retunrn a url
    
    Example.
    val youtubeJExtractor = YoutubeJExtractor()
    val url1 = youtubeJExtractor.getUrlForAndroidEmbeddedPlayer(youtubeVideoId)
    val url2 = youtubeJExtractor.getUrl(youtubeVideoId)



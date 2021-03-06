import org.apache.spark.streaming.{Seconds, StreamingContext}
import StreamingContext._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming.twitter._

/**
 * Calculates popular hashtags (topics) over sliding 10 and 60 second windows from a Twitter
 * stream. The stream is instantiated with credentials and optionally filters supplied by the
 * command line arguments.
 *
 */
object TwitterPopularTags {

  def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: sbt 'run " + "[filter1] [filter2] ... [filter n]"+ "'")
      System.exit(1)
    }

    val (master, filters) = (args(0), args.slice(5, args.length))

    // Twitter Authentication credentials
    System.setProperty("twitter4j.oauth.consumerKey", "O9OCPLFT5IDMqdqKFZKGcYpJn")
    System.setProperty("twitter4j.oauth.consumerSecret", "Rvil5TfqAp4ufMVr0vpOkKoLVUxUnJjN1Nsqzilnfe3p3wLgVW")
    System.setProperty("twitter4j.oauth.accessToken", "1919544354-J5HXw3P4AGdqQaJit5whdeVd1aZCeCRPwIjuDH8")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "Y724hhIf0R3dqxuFMYB7gJheGQaX8EVtZQPbQ15no3Z18")
    

    val ssc = new StreamingContext("local[2]", "TwitterPopularTags", Seconds(2),
      System.getenv("SPARK_HOME"), StreamingContext.jarOfClass(this.getClass))
    val stream = TwitterUtils.createStream(ssc, None, filters)    
    println("===Direct Stream Print===")
    // print the whole stream
    stream.foreachRDD(rdd=>rdd.foreach(println))
    
    // user details
    val users = stream.map(userJson => userJson.getUser())
    val usersCol = stream.map(user => user.getUser().getName()+ "@" + user.getUser().getLocation() + "@" + user.getUser().getFollowersCount() + "@" + user.getUser().getFriendsCount() + "@" + user.getUser().getFavouritesCount())
    println("===User Details===")
    usersCol.foreachRDD(rdd=>rdd.foreach(println))
    
    // tweet details
    val statuses = stream.map(tweet => tweet.getGeoLocation())
    println("===Tweet Details===")
    statuses.foreachRDD(rdd=>rdd.foreach(println))

    // getting the hashtags
    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))

    val topCounts60 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
                     .map{case (topic, count) => (count, topic)}
                     .transform(_.sortByKey(false))

    val topCounts10 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(10))
                     .map{case (topic, count) => (count, topic)}
                     .transform(_.sortByKey(false))

    // Print popular hashtags
    topCounts60.foreachRDD(rdd => {
      val topList = rdd.take(5)
      println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })

    topCounts10.foreachRDD(rdd => {
      val topList = rdd.take(5)
      println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
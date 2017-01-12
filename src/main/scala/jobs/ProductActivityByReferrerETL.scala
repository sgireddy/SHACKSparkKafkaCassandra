package jobs
import config.Settings._
import models.Activity
import _root_.kafka.serializer.StringDecoder
import com.datastax.spark.connector.streaming._
import org.apache.spark.SparkContext
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka.KafkaUtils
/** Created by Shashi Gireddy on 1/4/17 */
object ProductActivityByReferrerETL {

  def promoEfficiencyJob(sc: SparkContext, duration: Duration): StreamingContext = {
    val ssc = new StreamingContext(sc, duration)
    val topic = KafkaSettings.kafkaTopic
    val kafkaDirectParams = KafkaSettings.kafkaDirectParams
    val kafkaDirectStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaDirectParams, Set(topic)
    ).map(_._2)

    val userActivityStream = kafkaDirectStream.transform( input =>
      for {
        line <- input
        activity <- utils.tryParse[Activity](line)
      } yield activity
    )
    //.cache()
    //.saveToCassandra("promo", "activities")

    //checkpoint
//    sc.getCheckpointDir match {
//      case Some(dir) => ssc.checkpoint(dir)
//      case _ => println("Unable to set checkpoint")
//    }

    userActivityStream.saveToCassandra("promo", "activities")


    /*** Do Analytics on userActivityStream, maintain state, save snapshots to Cassandra as saving direct stream is impractical**/
    ssc
  }
}
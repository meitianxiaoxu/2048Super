package graduation.streaming

import org.apache.spark.streaming.dstream.InputDStream

import scala.reflect.ClassTag

/**
  * Created by KeXu on 2017/4/12.
  */
object AnalyzeStream extends Analyze {

  override def analyze[K: ClassTag, V: ClassTag](stream: InputDStream[(K, V)]): Unit = {

  }

}

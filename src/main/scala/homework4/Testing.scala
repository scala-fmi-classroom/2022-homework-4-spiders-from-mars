package homework4

import homework4.http.AsyncHttpClient
import homework4.math.Monoid
import homework4.processors.WordCount
import homework4.processors.WordCounter
import homework4.spiders.WordCounterSpider

import java.util.concurrent.ForkJoinPool
import scala.concurrent.{Await, ExecutionContext, duration}
import scala.concurrent.duration.{Duration, SECONDS}

object Testing:

  val testUrl = "http://localhost/81619_web_archive/about.php"
  val httpClient = new AsyncHttpClient

  val config = new SpideyConfig(
    maxDepth = 1,
    sameDomainOnly = false,
    tolerateErrors = true,
    retriesOnError = 1
  )

  val processingSpider = WordCounterSpider

  given ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool)

  val spidey = new Spidey(httpClient)

  def main(args: Array[String]) =
    println(Await.result(processingSpider.process(spidey, testUrl, 1)(config), Duration(1000, duration.SECONDS)))
    println("Testing complete")

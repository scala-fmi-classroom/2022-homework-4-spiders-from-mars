package homework4

import homework4.html.HtmlUtils
import homework4.http.*
import homework4.math.Monoid

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, duration}

case class SpideyConfig(
  maxDepth: Int,
  sameDomainOnly: Boolean = true,
  tolerateErrors: Boolean = true,
  retriesOnError: Int = 0
)

def monoidSum[O: Monoid](monoids: List[O]): O =
  monoids.tail.foldLeft(monoids.head)(_ |+| _)

class Spidey(httpClient: HttpClient)(using ExecutionContext):
  def crawl[O: Monoid](url: String, config: SpideyConfig)(processor: Processor[O]): Future[O] = Future {
    println("Making a request to: " + url)
    var response = Await.result(httpClient.get(url), Duration(5, duration.SECONDS))
    println("Response receieved from: " + url + " ; Length: " + response.body.length)
    println("Processing: " + url)
    var baseMonoid = Await.result(processor(url, response), Duration(200, duration.SECONDS))
    println("Completed Processing: " + url)

    if config.maxDepth > 0 then
      val futures = HtmlUtils
        .linksOf(
          response.body,
          url
        )
        .distinct
        .map((link: String) => crawlDeeper[O](config, processor)(link))

      baseMonoid |+| monoidSum(
        futures.map(
          Await.result(_, Duration(200, duration.SECONDS))
        )
      )
    else baseMonoid

  }

  def crawlDeeper[O: Monoid](config: SpideyConfig, processor: Processor[O])(url: String) =
    crawl(
      url,
      new SpideyConfig(
        maxDepth = config.maxDepth - 1,
        sameDomainOnly = config.sameDomainOnly,
        tolerateErrors = config.tolerateErrors,
        retriesOnError = config.retriesOnError
      )
    )(processor)

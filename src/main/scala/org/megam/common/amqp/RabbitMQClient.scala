/* 
** Copyright [2012-2013] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package org.megam.common.amqp

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.concurrent._
import java.util.concurrent.{ ThreadFactory, Executors }
import RabbitMQClient._
import java.util.concurrent.atomic.AtomicInteger
import com.rabbitmq.client._
import com.rabbitmq.client.Channel
import com.typesafe.config._
/**
 * @author ram
 *
 * Scalazified version of the RabbitMQ Java client. This follows the insipiration from stackmob's newman (ApacheHttpClient) fascade.
 * Uses the IO monad of scalaz and delays the execution until the unsafePerformIO is called.
 * Every invocation of the RabbitMQClient results in creating a connection. There is no connection pooling performed but rather
 * performed by the default impl. of ConnectionFactory. (This is something to-do, and watch for)
 * Two AMQP activities are supported by this client.
 * publish   : this uses the scalaz.concurrent feature to execute each of the publish operation in its own thread.
 * subscribe : this uses the scalaz.concurrent feature to execute each of the subscribe operation in its own thread.
 */
/*class RabbitMQClient(val connectionTimeout: Int = RabbitMQClient.DefaultConnectionTimeout,
  val maxChannels: Int = RabbitMQClient.DefaultChannelMax,
  val strategy: Strategy = Strategy.Executor(amqpThreadPool),
  uris: String, exchange: String, queue: String) extends AMQPClient {*/

 class RabbitMQClient(connectionTimeout: Int ,
  maxChannels: Int , strategy: Strategy ,
  uris: String, exchange: String, queue: String) extends AMQPClient {
  
   def this(uris: String, exchange: String, queue: String) = 
     this(RabbitMQClient.DefaultConnectionTimeout, RabbitMQClient.DefaultChannelMax, Strategy.Executor(amqpThreadPool), uris, exchange, queue)         

  /**
   * convert uris to an array of RabbitMQ's Address objects
   * Address(host, port) ...
   * If you have failover servers then feed them in the conf file.
   * As its lazy val, this gets evaluated only once when its first called and the
   * rest of the code just reuses the old evaluation.
   *
   */
println("Execute method")
  //val conf = ConfigFactory.load("~/code/megam/workspace/megam_common/src/main/resources/megam.conf")
  //println("Config-------------->"+conf)
  private lazy val urisToAddress: Array[Address] = {
    //val uriAddress = conf.getString("app.amqp.uri")  
    println(uris)
    val uri = uris.split(":")        
    val add = Array(new Address(uri(0), 5672))    
    add
  }

  /**
   * Connect to the rabbitmq system using the connection factory.
   */
  private val connManager: Connection = {
    val factory: ConnectionFactory = new ConnectionFactory()       
    val addrArr: Array[Address] = urisToAddress   
    val cm = factory.newConnection(addrArr)
    cm
  }

  /**
   *  Create a channel on the connection.
   *  Refer RabbitMQ Java guide for more info :  http://www.rabbitmq.com/api-guide.html#consuming
   */
  private val channel: Channel = connManager.createChannel()
   channel.exchangeDeclare(exchange, "direct", true)
    val queueName = channel.queueDeclare().getQueue()
    channel.queueBind(queueName, exchange, "key")
  /* channel.exchangeDeclare(exchangeName, "direct", true);
	channel.queueDeclare(queueName, true, false, false, null);
	channel.queueBind(queueName, exchangeName, routingKey);
    */

  /**
   * This function wraps an function (t => T) into  concurrent scalaz IO using a strategy.
   * The strategy is a threadpooled executors.
   * This mean any IO monad will be threadpooled when executed.
   */
  //private def wrapIOPromise[T](t: => T): IO[Promise[T]] = IO(Promise(t)(strategy))
private def wrapIOPromise[T](t: => T): IO[Promise[T]] = IO(Promise(t))
  protected def liftPublishOp(messages: Messages): IO[Promise[AMQPResponse]] = wrapIOPromise {
    println(messages)   
    messages.foreach { list: NonEmptyList[(String, String)] =>
      list.foreach { tup: (String, String) =>
        //  if (!tup._1.equalsIgnoreCase(CONTENT_LENGTH)) {
        //httpMessage.addHeader(tup._1, tup._2)
        //  }
      }
    }

    /**
     * call the basicPublish and return the results.
     *
     * val rabbitResp = ???
     *
     * Split the results as deemed fit. Fit it in the AMQPResponse as a tuple.
     *  val responseCode = rabbitResponse.getAllHeaders.map(h => (h.getName, h.getValue)).toList
     *  val responseBody = Option(rabbitResponse.getEntity).map(new BufferedHttpEntity(_)).map(EntityUtils.toByteArray(_))
     */
    val responseCode = ???
    val responseBody = ???
    AMQPResponse(responseCode, responseBody)
  }

  protected def liftSubscribeOp(messages: Messages): IO[Promise[AMQPResponse]] = wrapIOPromise {
    messages.foreach { list: NonEmptyList[(String, String)] =>
      list.foreach { tup: (String, String) =>
        //  if (!tup._1.equalsIgnoreCase(CONTENT_LENGTH)) {
        //httpMessage.addHeader(tup._1, tup._2)
        //  }
      }
    }

    /**
     * call the basicPublish and return the results.
     *
     * val rabbitResp = ???
     *
     * Split the results as deemed fit. Fit it in the AMQPResponse as a tuple.
     *   boolean autoAck = false;
     * channel.basicConsume(queueName, autoAck, "myConsumerTag",
     * new DefaultConsumer(channel) {
     * @Override
     * public void handleDelivery(String consumerTag,
     * Envelope envelope,
     * AMQP.BasicProperties properties,
     * byte[] body)
     * throws IOException
     * {
     * String routingKey = envelope.getRoutingKey();
     * String contentType = properties.contentType;
     * long deliveryTag = envelope.getDeliveryTag();
     * // (process the message components here ...)
     * channel.basicAck(deliveryTag, false);
     * }
     * });
     *
     *    * Split the results as deemed fit. Fit it in the AMQPResponse as a tuple.
     *  val responseCode = rabbitResponse.getAllHeaders.map(h => (h.getName, h.getValue)).toList
     *  val responseBody = Option(rabbitResponse.getEntity).map(new BufferedHttpEntity(_)).map(EntityUtils.toByteArray(_))
     */
    val responseCode = ???
    val responseBody = ???
    AMQPResponse(responseCode, responseBody)

  }

  /**
   * byte[] messageBodyBytes = "Hello, world!".getBytes();
   *
   */

  override def publish(m1: Messages, m2: Messages): PublishRequest = new PublishRequest {
    override val messages = m1
    override def prepareAsync: IO[Promise[AMQPResponse]] = liftPublishOp(m1)
  }

  override def subscribe(m1: Messages, m2: Messages): SubscribeRequest = new SubscribeRequest {
    override val messages = m1
    override def prepareAsync: IO[Promise[AMQPResponse]] = liftSubscribeOp(m1)

  }

}

object RabbitMQClient {

  private[RabbitMQClient] val DefaultConnectionTimeout = ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT
  private[RabbitMQClient] val DefaultChannelMax = ConnectionFactory.DEFAULT_CHANNEL_MAX

  private val threadNumber = new AtomicInteger(1)
  lazy val amqpThreadPool = Executors.newCachedThreadPool(new ThreadFactory() {

    override def newThread(r: Runnable): Thread = {
      new Thread(r, "megam_amqp-" + threadNumber.getAndIncrement)
    }
  })
}
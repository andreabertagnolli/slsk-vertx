package ndr.brt.slsk.server

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.net.NetClient
import ndr.brt.slsk.LoginRequested
import ndr.brt.slsk.LoginResponded
import ndr.brt.slsk.emit
import ndr.brt.slsk.on
import org.slf4j.LoggerFactory

class ServerListener(
        private val serverHost: String,
        private val serverPort: Int,
        server: NetClient,
        private val eventBus: EventBus,
        callback: (AsyncResult<ServerListener>) -> Unit) {

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        log.info("Starting Server Listener")
        server.connect(serverPort, serverHost) {
            if (it.succeeded()) {
                log.info("Connected with server {}:{}", serverHost, serverPort)
                ServerSocketHandler(eventBus).handle(it.result())
                callback(Future.succeededFuture(this))
            } else {
                log.error("Connection with server failed", it.cause())
                callback(Future.failedFuture(it.cause()))
            }
        }
    }

    fun login(username: String, password: String, callback: (LoginResponded) -> Unit) {
        log.info("Login with username {}", username)
        eventBus.on(LoginResponded::class, callback::invoke)
        eventBus.emit(LoginRequested(username, password))
    }

}
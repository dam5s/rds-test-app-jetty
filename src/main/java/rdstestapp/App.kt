package rdstestapp

import com.mysql.cj.jdbc.MysqlDataSource
import com.zaxxer.hikari.HikariDataSource
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

class App {

    private fun dataSource(): DataSource {
        val mysqlDataSource = MysqlDataSource().apply {
            setURL("${env("JDBC_URL")}?useSSL=true&requireSSL=true&verifyServerCertificate=true")
            user = env("JDBC_USER")
            setPassword(env("JDBC_PASSWORD"))
        }

        return HikariDataSource().apply {
            dataSource = mysqlDataSource
            connectionTestQuery = "select case when @@innodb_read_only = 0 then 1 else (select table_name from information_schema.tables) end as `1`"
            maximumPoolSize = 2
        }
    }

    private fun env(name: String) = System.getenv(name)
        ?: throw IllegalStateException("Need env variable $name to be defined")

    private val serverPort = System.getenv("PORT")!!.toInt()
    private val server = Server(serverPort)


    fun run() {
        server.handler = HomeHandler(dataSource())
        server.stopAtShutdown = true
        server.start()
    }
}


class HomeHandler(private val dataSource: DataSource) : AbstractHandler() {

    override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) = try {
        val body =
            dataSource.connection.use { connection ->
                val stmt = connection.prepareStatement("SELECT COUNT(1) FROM terms_and_conditions_acceptance")
                val rs = stmt.executeQuery()

                if (rs.next()) rs.getLong(1)
                else -1
            }

        baseRequest.isHandled = true
        response.writer.write(body.toString())
    } catch (e: Throwable) {
        System.err.println(e.message)
        e.printStackTrace()
    }
}


fun main() {
    App().run()
}

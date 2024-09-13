import com.diconium.mobile.tools.kebabkrafter.sample.MockServices
import com.diconium.mobile.tools.kebabkrafter.sample.RealServices
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.installGeneratedRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

fun main() {
	embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
	install(ContentNegotiation) {
		json(jsonSerialization)
	}
	routing {
		// the real implementation of the sample backend
		route("/api") {
			installGeneratedRoutes(RealServices)
		}

		// a mock implementation of the sample backend
		route("/mock") {
			installGeneratedRoutes(MockServices)
		}

		// ktor can also be serving its own swagger API
		route("/openapi") {
			install(CORS) {
				anyHost()
				allowHeader(HttpHeaders.ContentType)
			}
			swaggerUI(path = "", swaggerFile = "petstore/swagger.yml")
			staticResources("/schemas", "petstore/schemas")
		}
	}
}

@OptIn(ExperimentalSerializationApi::class)
private val jsonSerialization = Json {
	explicitNulls = false
	ignoreUnknownKeys = true
}

<p align="center">
  <a href="docs/kebab-krafter-v2.png" rel="noopener">
    <img width=200px height=200px src="docs/kebab-krafter-v2.png" alt="Project logo"></a>
</p>

<h3 align="center">Kebab Krafter</h3>
<h6 align="center">FOSS made with ❤️ in Berlin</h6>

<p align="center"> Generates all the boring network API code from a Swagger spec.
    <br>
</p>
<p>
Available for:
    <br> - Ktor Server
    <br> - Ktor Client (soon) 
    <br> - Swift client (hopefully)
</p>

[//]: # (Alternative logos)
[//]: # (<p align="center">)

[//]: # (  <a href="docs/kebab-krafter-0.png" rel="noopener">)

[//]: # (    <img width=200px height=200px src="docs/kebab-krafter-0.png" alt="Project logo"></a>)

[//]: # (</p>)
[//]: # (<p align="center">)

[//]: # (  <a href="docs/kebab-krafter-1.png" rel="noopener">)

[//]: # (    <img width=200px height=200px src="docs/kebab-krafter-1.png" alt="Project logo"></a>)

[//]: # (</p>)

[//]: # ()
[//]: # (<p align="center">)

[//]: # (  <a href="docs/kebab-krafter-2.png" rel="noopener">)

[//]: # (    <img width=200px height=200px src="docs/kebab-krafter-2.png" alt="Project logo"></a>)

[//]: # (</p>)

[//]: # ()
[//]: # (<p align="center">)

[//]: # (  <a href="docs/kebab-krater-11.png" rel="noopener">)

[//]: # (    <img width=200px height=200px src="docs/kebab-krater-11.png" alt="Project logo"></a>)

[//]: # (</p>)

[//]: # ()
[//]: # (<p align="center">)

[//]: # (  <a href="docs/kebab-krafter-22.png" rel="noopener">)

[//]: # (    <img width=200px height=200px src="docs/kebab-krafter-22.png" alt="Project logo"></a>)

[//]: # (</p>)

---

## 📝 Table of Contents

- [About](#about)
- [Getting Started](#getting_started)
- [Documentation](docs/index.htm)
- [Deployment](#deployment)
- [Usage](#usage)
- [Contributing](CONTRIBUTING.md)
- [Authors](#authors)
- [Acknowledgments](#acknowledgement)

## 🧐 About <a name = "about"></a>

A gradle plugin to auto-generate network interfaces from a set of swagger API documentation.

## 🏁 Getting Started <a name = "getting_started"></a>

To start using the plugin just add to your `build.gradle.kts` file:

```kotlin
id("com.diconium.mobile.tools.kebab-krafter") version "version"
```

### Generate Ktor Server

Just add the configuration to your gradle script

```kotlin

ktorServer {

	// Root package name for the generated code
	packageName = "root.package.name.for.the.generated.code"

	// file system location for the swagger spec
	specFile = File(rootDir, "swagger/api.yml")

	// definition for the receiver class for the API controllers
	contextSpec {
		packageName = "com.myserver.api"
		className = "CallScope"
		factoryName = "from"
	}
}
```

From the example above the a `InstallRoutes.kt` is generated in the `root.package.name.for.the.generated.code` with an extension function for Ktor `fun Route.installGeneratedRoutes(locator: ServiceLocator)`

It also generates all the necessary `data class` models and controller `interfaces` that satisfy the API in a nicely unit testable way.

The `contextSpec` is a joker card to extract any metadata needed from the `Ktor.ApplicationCall` before passing to the controller. In the sample app you can see it extracting the `accept-language` header into `Locale` object. Making the context an interface is advisable, so that it's trivial to unit test the controller by creating a `FakeContext()`

The `ServiceLocator` is a very simple `get<T>` interface that can be adapted to any dependency injection you want to use. For example using Koin it would be something like:

```kotlin
class KoinServiceLocator(private val koin: Koin) : ServiceLocator {
	override fun <T : Any> getService(type: KClass<T>): T = koin.get(type)
}
```

### Implement the Server

With that generated code in place, all you gotta do is implement the interfaces and install the routes on the Ktor instance.

Check the `sample/` app with the "Pet Store" for a full example.

## 🔧 Running the tests <a name = "tests"></a>

Explain how to run the automated tests for this system.

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## 🎈 Usage <a name="usage"></a>

Add notes about how to use the system.

## Documentation

Please see [documentation guide](https://docs)

## 🚀 Deployment <a name = "deployment"></a>

Add additional notes about how to deploy this on a live system.

## ✍️ Authors <a name = "authors"></a>

- [@rvp-diconium](https://github.com/rvp-diconium)

See also the list of [contributors](https://github.com/diconium/mcc-network-generator/contributors) who participated in
this project.

## 🎉 Acknowledgements <a name = "acknowledgement"></a>

- Hat tip to anyone whose code was used
- Inspiration
- References

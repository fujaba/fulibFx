# Setup

In order to properly test your application, it is recommended to use [TestFX](https://github.com/TestFX/TestFX) alongside [Mockito](https://github.com/mockito/mockito).
For a full explanation of both libraries, checkout their official documentation, as the following documentation will only cover a small part of what the projects have to offer.
## TestFX

TestFX can be used to test the frontend of your application by checking if certain requirements are met, for example view elements being visible or having a certain property.

Alongside TestFX, we also include Monocle which allows for headless testing without the app having to be open on your screen every time the tests are ran.

```groovy
    testImplementation group: 'org.testfx', name: 'testfx-junit5', version: testFxVersion
    testImplementation group: 'org.testfx', name: 'openjfx-monocle', version: monocleVersion
```

To enable headless testing, the following lines can be added to your `test` gradle task:

```groovy
test {
    // ...
    if (hasProperty('headless') || System.getenv('CI')) {
        systemProperties = [
                'java.awt.headless': 'true',
                'testfx.robot'     : 'glass',
                'testfx.headless'  : 'true',
                'glass.platform'   : 'Monocle',
                'monocle.platform' : 'Headless',
                'prism.order'      : 'sw',
                'prism.text'       : 't2k',
        ]
    }
}
```

Whenever the tests are ran with `CI=true`, headless mode will be enabled allowing for testing in CI environments like GH Actions.

## Mockito

Mockito is used to redefine certain methods in the code which currently aren't being tested but could influence the test results, for example by accessing an external API.

```groovy
testImplementation group: 'org.mockito', name: 'mockito-junit-jupiter', version: mockitoVersion
```

---

[Overview](README.md) | [Testing Controllers ➡](2-controllers)

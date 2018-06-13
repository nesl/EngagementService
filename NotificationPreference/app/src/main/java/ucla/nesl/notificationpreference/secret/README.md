# Secret.java
---

There is some information we intend to hide for security reason, and this will not be committed to the GitHub. However, we'd like to share the template of `Secret.java` here.

### Template of Secret.java

```java
public class Secret {
    public static final String serverURL = "...";
    public static final int serverPort = ???;

    public String getServerURLWithPage(String page) {
        return String.format(Locale.getDefault(), "https://%s:%d/%s/", serverURL, serverPort, page);
    }
}
```


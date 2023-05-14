# Aqua Input #

Aqua Input is a tool for the Spigot API, meant to simplify the process of
getting player input of chat by providing a system of defining custom
type of inputs.

# How to use

In order to use Aqua Input in your projects, you'll need to install the
maven repository locally on your machine.

1. Firstly, start by cloning the repository on your machine.
2. After the cloning is done, run ```mvn clean install```
3. Lastly, you can include the tool as a dependency in your projects.

```xml
<dependency>
    <groupId>dev.cristike</groupId>
    <artifactId>aquainput</artifactId>
    <version>VERSION</version> 
    <scope>compile</scope>
</dependency>

<!-- The latest version is 1.1.0 -->
```

Keep in mind that you'll need to reinstall the repository when a
new version of Aqua is released and also shade it into your plugin.

# Documentation

In order to understand the tool better, you can check out the [docs](https://cristike.github.io/AquaInput/).

# Example

Let's say that we need to get two integers from a player when he joins the server.
We also want to restrict him from moving, interacting with anything and
execute commands except msg, and also send useful messages to him.  

To create this logic, we'll start by creating a class that extends the ```AquaInput``` class, that
is responsible for all the logic presented above besides the checking
of the input. In this class we'll override the ```isValidInput()``` function and
check if there are two numbers separated by spaces.

```java
public class TwoIntegerInput extends AquaInput {

    @Override
    public boolean isValidInput(@NotNull String input) {
        String[] words = input.split(" ");
        
        if (words.length != 2) return false;
        
        try {
            Integer.parseInt(words[0]);
            Integer.parseInt(words[1]);
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```  

Next, in our ```JoinEvent``` class we will create a new instance of this class,
and prompt input from the player. We won't bother parsing the final input
into integers for the sake of simplicity, but keep in mind that the input prompt
response is a String. The code below shows the event handler.

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    TwoIntegerInput input = new TwoIntegerInput();

    input.setFlags(InputFlag.DISABLE_MOVEMENT,InputFlag.DISABLE_INTERACTION, InputFlag.DISABLE_COMMANDS)
            .setAllowedCommands("msg")
            .setMessage(InputMessage.INVALID_INPUT, INVALID_INPUT_MESSAGE)
            .setMessage(InputMessage.DISABLED_COMMANDS, DISABLED_COMMANDS_MESSAGE)
            .setMessage(InputMessage.DISABLED_INTERACTION, DISABLED_INTERACTION_MESSAGE)
            .setMessage(InputMessage.SUCCESS, ChatColor.GREEN + "SUCCESS!");

    AquaInputManager.promptInput(plugin, player.getUniqueId(), input).thenAccept(r -> {
        if (r.status() == InputStatus.SUCCESS)
            player.sendMessage("INPUT: " + r.value());
    });
}
```

# Contact

If you have any questions regarding Aqua Input, feel free to contact me on discord:
```Cristike#2808```.

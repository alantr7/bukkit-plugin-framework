# Overview
BPF is a Minecraft plugin framework that allows easier and quicker plugin development.

## What does this allow me?
BPF allows easier creation of commands, tab-complete, GUIs, permissions, etc. Also, annotations are another big part of BPF. Read more about annotations below.

### Commands
BPF allows easier creation of commands, their permissions, handling parameters, arguments, etc. Take a look at the example below:
```java
@CommandHandler
public Command greetPlayer = CommandBuilder.using("greet")
  .permission(Permissions.GREET)
  .parameter("{player}", p -> p
    .defaultValue(c -> null)
    .tabComplete(args -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList())
    .evaluator(Evaluator.PLAYER)
  .executes(context -> {
    Player target = context.optArgument("player");
    if (target == null) {
      context.respond("Player not found!");
      return;
    }

    target.sendMessage("Greetings from " + context.getExecutor().getName() + "!");
    context.respond("You have greeted " + target.getName() + "!");
  });
```

### GUIs
BPF comes with bpf-gui module which provides easier creation of GUIs. You can easily set items, bind actions to items, add open/close event handlers, refill, enable/disable item clicking/dragging, handle clicking outside of GUI, etc.
Here's an example of a confirmation GUI.
```java
public class ConfirmationGUI extends GUI {

  @Getter
  private Result result;

  public ConfirmationGUI(BukkitPlugin plugin, Player player) {
    super(plugin, player);
  }

  @Override
  protected void init() {
    createInventory("Are you sure?", 27);
    setInteractionEnabled(false);
  }

  @Override
  protected void fill(Inventory inventory) {
    setItem(11, new ItemStack(Material.RED_STAINED_GLASS_PANE));
    registerInteractionCallback(11, ClickType.LEFT, () -> {
      result = Result.CANCELED;
      close();
    });

    setItem(13, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
    registerInteractionCallback(13, ClickType.LEFT, () -> {
      result = Result.CONFIRMED;
      close();
    });
  }

  @Override
  protected void onInventoryClose(CloseInitiator initiator) {
    if (initiator != CloseInitiator.SELF) {
      result = Result.CANCELED;
    }
  }

}
```

### Annotations
There are several annotations provided by BPF. There are 3 types of annotations: Normal, Filter and Generative annotations.
- Normal annotations are annotations that are like "shortcuts" for your code. For example, if your class has @Singleton annotation, and methods annotated with @EventHandler,
these events will be automatically registered instead of using `Bukkit.getPluginManager().registerEvents(...)` and you won't have to manage that class. It will be stored and reused automatically.
- Filter annotations are annotations that determine whether the class, fields and methods can be processed.
- Generative annotations are annotations used mostly to generate the `plugin.yml` file.

Normal and filter annotations:
- **@Singleton** annotation is used for automatic instantiation of the annotated class. Singleton is used only when there's 1 instance of that class needed, like event listeners, command executors, etc.
- **@Inject** annotation is used together with @Singleton. @Inject will automatically insert the instantiated class into the field (class is found based on the field's type)
- **@RequiresPlugin** is a filter annotation. It can be used on classes, fields and methods
- **@Invoke** annotation is used for automatically invoking annotated methods on a specific occasion (plugin enable or disable)
- **@InvokePeriodically** is similar to @Invoke, but as it name tells it invokes the method on the specified timer. It's equivalent to `Scheduler#runTaskTimer`
- **@CommandHandler** is used to automatically register annotated command
- **@EventHandler** annotation belongs to Bukkit but is processed by our bean-manager. Events will be automatically registered

Generative annotations:
- **@JavaPlugin** annotation generates "name", "main", "description" and "version" properties for `plugin.yml`
- **@Depends** generates the "depends" section
- **@SoftDepends** generates the "softdepends" section
- **@Command** generates command definition with its "description" and "permission" properties
- **@Permission** generates permission definition with "default" and "description" properties

Example of using annotations:
```java

@JavaPlugin(name = "CodeBots", version = "1.2.1")
@SoftDepends("Vault")
public class CodeBotsPlugin extends BukkitPlugin {

  @Override
  public void onPluginEnable() {
    logger.info("Plugin has disabled!");
  }

  @Override
  public void onPluginDisable() {
    logger.info("Plugin has disabled!");
  }

}

@Singleton
public class Events {

  @Inject
  Logger logger;

  @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
  void logInstantiation() {
    logger.info("Event listener instantiated.");
  }

  @EventHandler
  void onPlayerJoin(PlayerJoinEvent e) {
    logger.info(e.getPlayer().getName() + " joined!");
  }

}

```

package us.jcedeno.condor.paper;

import com.microsoft.azure.management.compute.DiskSkuTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import org.apache.commons.lang.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import rx.Observable;

@RequiredArgsConstructor
@CommandAlias("azure")
public class GuiCommand extends BaseCommand {

    final static String JCEDENO_SSH_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCxywToqUnBdWNU/DmUm0+wal/xLtdVDStKT3iEB736UaXQKmL4xUyGq/iaD6xyvpe/oaS7cGFmHECumluj0bKWpSFIsVAmGhl1GzMR7i49k0q92abyJ8+wa8R//ysvJvDIX3hrdPch7l0tkCsr3CUzjHnli7QELJJFtF5jwZk0UsAITyLaQYlAjY+Ta8weSPE8O80Z3VJ5e3FbJlnoyXHLidNsCNuJTX4ZpfT0GkJYoj3oYQ03XC2eTwYaGZvQHc4sKj0ExZbC0XBJL9qP4xOX0wqng1fvz9zICIRz4JKmrwrX1MRRF54rLrHhj7gVazTDsAosGbTFeU2+pXXE6fsMIr3S5mSMCwAXBzAR08CbjiV1qxxfOtEwrxaqdipjNpGapoLjHs6rgiXuUBNKW7D+lr96GMbXnYhXrnpqDd2/ztjdeq3sroytopfHc6fAm/eWq91GzzErvAKMDRjRFocW4h8H8ccNwxg/S0XK8TcYx7TNfco7k/YBS8luClgx+Jk= henixceo@gmail.com";
    final static String ALEIV_SSH_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDWMkzRAiNqxNXXAfi4aE/6bJ7VeE0WsW1lwistm41zgPpMZFcarv901OtMv+VyTURxO4R3g18b326p5JbR0tO3BMp3vqCdmtA7xrTreGCcz5sB+GUvkMMyV5koY9t8PRuOh4hnvoeAEdJ7R2WzswD33idjViSFHK4WwCjN61fXs5VUVdyNZiaW6nnEP7bSCHMxghtpURZSmcNUa34FHWARPnQIeqFTycLdI/1B/PEypwAEFZAA35Rsb51bxF6K1agDYuc1mW4DaH6xDoaUZNAI0T73RfYJMW+7l4ILXr2W+HjHpAsgsn3bOXxgj35xxDbV7rmdIFOL4S9JE3eUbVKb jorgefcorralesmayorga@iMac-de-Jorge.local";
    final static String RESOURCE_GROUP = "hynix-resources";
    final static String HYNIX_IMAGE = "/subscriptions/fcb53ea3-4077-45a3-9a6d-c8fd42c64372/resourceGroups/hynix-resources/providers/Microsoft.Compute/images/debian-10-graal-hynix";
    final static String YATOPIA_IMAGE = "/subscriptions/fcb53ea3-4077-45a3-9a6d-c8fd42c64372/resourceGroups/hynix-resources/providers/Microsoft.Compute/images/uhc-image";
    private @NonNull CondorPaper instance;

    @Default
    public void cmd(Player sender) {
        final var inv = new FastInv(4 * 9, ChatColor.BLUE + "Azure GUI");
        inv.open(sender);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            instance.getAzure().virtualMachines().listAsync().doOnNext((c) -> {
                inv.addItem(
                        new ItemBuilder(Material.NETHER_STAR)
                                .addLore(ChatColor.WHITE + "Instance name: " + c.computerName(),
                                        ChatColor.WHITE + "Type: " + c.size().toString(),
                                        ChatColor.WHITE + "Power: " + c.powerState().toString(),
                                        ChatColor.WHITE + "Status: "
                                                + c.instanceView().statuses().get(0).displayStatus(),
                                        ChatColor.WHITE + "IP: " + c.getPrimaryPublicIPAddress().ipAddress())
                                .name(ChatColor.GOLD + "VM " + c.name()).build(),
                        e -> {
                            sender.closeInventory();
                            sender.sendMessage("Deleting VM " + c.name() + "...");
                            Bukkit.getScheduler().runTaskLater(instance, () -> cmd(sender), 40);
                            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                                instance.getAzure().virtualMachines().deleteByIdAsync(c.id()).toObservable()
                                        .doOnNext((a) -> sender.sendMessage(a.getClass().getSimpleName() + " update"))
                                        .doOnCompleted(
                                                () -> sender.sendMessage("Instance has been deleted " + c.name()))
                                        .subscribe();
                            });

                        });

            }).subscribe();

        });
    }

    @Subcommand("create")
    @CommandAlias("vmc")
    public void createInstance(CommandSender sender, String size, String vmName) {
        sender.sendMessage("Creating vm " + vmName);

        var stopWatch = new StopWatch();
        stopWatch.start();

        getHynixVirtualNetworkAsync().doOnNext((network) -> {
            Bukkit.broadcastMessage("Obtained network = " + network.name() + ". Creating VM now...");
            instance.getAzure().virtualMachines().define(vmName).withRegion(Region.US_EAST)
                    .withExistingResourceGroup(RESOURCE_GROUP).withExistingPrimaryNetwork(network).withSubnet("DEFAULT")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(instance.getAzure().publicIPAddresses().define(vmName)
                            .withRegion(Region.US_EAST).withExistingResourceGroup(RESOURCE_GROUP))

                    .withLinuxCustomImage(YATOPIA_IMAGE).withRootUsername("hynix").withSsh(JCEDENO_SSH_KEY)
                    .withSsh(ALEIV_SSH_KEY).withComputerName(vmName)
                    .withNewDataDisk(instance.getAzure().disks().define(vmName + "Disk").withRegion(Region.US_EAST)
                            .withExistingResourceGroup(RESOURCE_GROUP).withData().withSizeInGB(20)
                            .withSku(DiskSkuTypes.PREMIUM_LRS))
                    .withSize(size).createAsync().doOnNext((vmIndex) -> {
                        if (vmIndex instanceof VirtualMachine) {
                            var vm = (VirtualMachine) vmIndex;
                            var ip = vm.getPrimaryPublicIPAddress().ipAddress();
                            stopWatch.stop();
                            Bukkit.broadcastMessage("Instance creation took " + (stopWatch.getTime() / 1000.0) + "s");
                            var component = new ComponentBuilder("Instance IP: ")
                                    .append(vm.getPrimaryPublicIPAddress().ipAddress()).color(ChatColor.GOLD)
                                    .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ip))
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy IP!")))
                                    .create();
                            Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(component));
                        }

                    }).doOnError((e) -> {
                        sender.sendMessage("An error occured attempting to create " + vmName + ".\n" + e.getMessage());
                    }).subscribe();

        }).subscribe();
    }

    @Subcommand("types")
    public void createIp(CommandSender sender) {
        sender.sendMessage("Available instances: ");
        instance.getAzure().virtualMachines().sizes().listByRegion(Region.US_EAST).stream()
                .filter(all -> all.memoryInMB() <= 32000 && all.numberOfCores() <= 8 && all.numberOfCores() >= 1)
                .forEach(all -> sender
                        .sendMessage(all.name() + " " + all.numberOfCores() + "vCPU " + all.memoryInMB() + "Mb RAM"));
    }

    // Obtain the hynix virtual network protected by the network security group.
    Observable<Network> getHynixVirtualNetworkAsync() {
        return instance.getAzure().networks().getByIdAsync(
                "/subscriptions/fcb53ea3-4077-45a3-9a6d-c8fd42c64372/resourceGroups/hynix-resources/providers/Microsoft.Network/virtualNetworks/hynix-virtual-network");
    }

}

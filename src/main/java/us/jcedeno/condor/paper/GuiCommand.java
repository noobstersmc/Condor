package us.jcedeno.condor.paper;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

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

@RequiredArgsConstructor
@CommandAlias("azure")
public class GuiCommand extends BaseCommand {

    final static String SSH_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCxywToqUnBdWNU/DmUm0+wal/xLtdVDStKT3iEB736UaXQKmL4xUyGq/iaD6xyvpe/oaS7cGFmHECumluj0bKWpSFIsVAmGhl1GzMR7i49k0q92abyJ8+wa8R//ysvJvDIX3hrdPch7l0tkCsr3CUzjHnli7QELJJFtF5jwZk0UsAITyLaQYlAjY+Ta8weSPE8O80Z3VJ5e3FbJlnoyXHLidNsCNuJTX4ZpfT0GkJYoj3oYQ03XC2eTwYaGZvQHc4sKj0ExZbC0XBJL9qP4xOX0wqng1fvz9zICIRz4JKmrwrX1MRRF54rLrHhj7gVazTDsAosGbTFeU2+pXXE6fsMIr3S5mSMCwAXBzAR08CbjiV1qxxfOtEwrxaqdipjNpGapoLjHs6rgiXuUBNKW7D+lr96GMbXnYhXrnpqDd2/ztjdeq3sroytopfHc6fAm/eWq91GzzErvAKMDRjRFocW4h8H8ccNwxg/S0XK8TcYx7TNfco7k/YBS8luClgx+Jk= henixceo@gmail.com";
    final static String RESOURCE_GROUP = "hynix-resources";
    private @NonNull CondorPaper instance;

    @Default
    public void cmd(Player sender) {
        var inv = new FastInv(4 * 9, ChatColor.BLUE + "Azure GUI");
        inv.open(sender);
        var machines = instance.getAzure().virtualMachines().list();
        for (var vm : machines) {
            inv.addItem(new ItemBuilder(Material.NETHER_STAR)
                    .addLore(ChatColor.WHITE + "Instance name: " + vm.computerName(),
                            ChatColor.WHITE + "Type: " + vm.size().toString(),
                            ChatColor.WHITE + "Power: " + vm.powerState().toString(),
                            ChatColor.WHITE + "Status: " + vm.instanceView().statuses().get(0).displayStatus(),
                            ChatColor.WHITE + "IP: " + vm.getPrimaryPublicIPAddress().ipAddress())
                    .name(ChatColor.GOLD + "VM " + vm.name()).build());
        }

    }

    @Subcommand("create")
    @CommandAlias("vmc")
    public void createInstance(CommandSender sender, String vmName) {
        sender.sendMessage("Creating vm " + vmName);
        var vm = instance.getAzure().virtualMachines().define(vmName).withRegion(Region.US_EAST)
                .withExistingResourceGroup(RESOURCE_GROUP)
                .withExistingPrimaryNetworkInterface(getInterface(vmName + "fromMc"))
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS).withRootUsername("hynix")
                .withSsh(SSH_KEY).withComputerName(vmName).withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
                .createAsync().doAfterTerminate(() -> {
                    sender.sendMessage("Instance " + vmName + " has been created.");
                }).doOnError((e) -> {
                    sender.sendMessage("An error occured attempting to create " + vmName + ".\n" + e.getMessage());
                }).subscribe();
    }

    NetworkInterface getInterface(String name) {
        return instance.getAzure().networkInterfaces().define(name + "Interface").withRegion(Region.US_EAST)
                .withExistingResourceGroup(RESOURCE_GROUP).withExistingPrimaryNetwork(getNetwork(name))
                .withSubnet(name + "Subnet").withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(getNewIp(name)).create();
    }

    Network getNetwork(String name) {
        return instance.getAzure().networks().define(name + "Network").withRegion(Region.US_EAST)
                .withExistingResourceGroup(RESOURCE_GROUP).withAddressSpace("10.0.0.0/16")
                .withSubnet(name + "Subnet", "10.0.0.0/24").create();
    }

    PublicIPAddress getNewIp(String name) {
        return instance.getAzure().publicIPAddresses().define(name + "-ip").withRegion(Region.US_EAST)
                .withExistingResourceGroup(RESOURCE_GROUP).withDynamicIP().create();
    }

}

package top.mores.armory.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import top.mores.armory.catalog.LoadoutCatalog;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class LoadoutCatalogConfigLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // 文件名和目录分开，最终路径：config/taczloadout/armory.json
    private static final String DIR_NAME = "armory";
    private static final String FILE_NAME = "armory.json";

    private LoadoutCatalogConfigLoader() {
    }

    public record LoadResult(Path path, int gunCount, int attachmentCount, List<String> warnings) {
    }

    private static final class Root {
        int version = 1;
        List<GunJson> guns = new ArrayList<>();
        List<AttachmentJson> attachments = new ArrayList<>();
    }

    private static final class GunJson {
        String key;
        String name;
        String gunId;
    }

    private static final class AttachmentJson {
        String key;
        String name;
        String type;
        String attachmentId;

        // 可选
        Integer magBonus;
        Integer magCapacityOverride;
    }

    public static LoadResult reload() {
        Path path = getConfigPath();

        try {
            ensureDefaultFile(path);

            Root root;
            try (Reader reader = Files.newBufferedReader(path)) {
                root = GSON.fromJson(reader, Root.class);
            }

            if (root == null) root = new Root();
            if (root.guns == null) root.guns = new ArrayList<>();
            if (root.attachments == null) root.attachments = new ArrayList<>();

            List<String> warnings = new ArrayList<>();
            List<LoadoutCatalog.GunOption> guns = new ArrayList<>();
            List<LoadoutCatalog.AttachmentOption> attachments = new ArrayList<>();

            Set<ResourceLocation> usedGunKeys = new HashSet<>();
            for (GunJson gun : root.guns) {
                ResourceLocation key = parseRl(gun.key, "gun.key", warnings);
                ResourceLocation itemId = parseRl(gun.gunId, "gun.item", warnings);
                String name = safeText(gun.name);

                if (key == null || itemId == null || name.isBlank()) {
                    warnings.add("跳过无效枪械项: " + String.valueOf(gun != null ? gun.key : "null"));
                    continue;
                }
                if (!usedGunKeys.add(key)) {
                    warnings.add("重复枪械 key: " + key);
                    continue;
                }

                guns.add(new LoadoutCatalog.GunOption(key, name, itemId));
            }

            Set<ResourceLocation> usedAttachmentKeys = new HashSet<>();
            for (AttachmentJson attachment : root.attachments) {
                ResourceLocation key = parseRl(attachment.key, "attachment.key", warnings);
                ResourceLocation itemId = parseRl(attachment.attachmentId, "attachment.item", warnings);
                String name = safeText(attachment.name);
                AttachmentType type = parseAttachmentType(attachment.type, warnings);

                if (key == null || itemId == null || name.isBlank() || type == null || type == AttachmentType.NONE) {
                    warnings.add("跳过无效配件项: " + String.valueOf(attachment != null ? attachment.key : "null"));
                    continue;
                }
                if (!usedAttachmentKeys.add(key)) {
                    warnings.add("重复配件 key: " + key);
                    continue;
                }

                attachments.add(new LoadoutCatalog.AttachmentOption(
                        key,
                        name,
                        type,
                        itemId,
                        attachment.magBonus,
                        attachment.magCapacityOverride
                ));
            }

            LoadoutCatalog.replaceAll(guns, attachments);

            LOGGER.info("Armory 目录重载完成: guns={}, attachments={}, file={}",
                    guns.size(), attachments.size(), path);

            for (String warning : warnings) {
                LOGGER.warn("[Armory] {}", warning);
            }

            return new LoadResult(path, guns.size(), attachments.size(), warnings);
        } catch (IOException | JsonSyntaxException e) {
            throw new RuntimeException("加载军械库配置失败: " + path + " -> " + e.getMessage(), e);
        }
    }

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get()
                .resolve(DIR_NAME)
                .resolve(FILE_NAME);
    }

    private static void ensureDefaultFile(Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }

        Files.createDirectories(path.getParent());

        Root root = new Root();

        GunJson gun = new GunJson();
        gun.key = "armory:m4a1";
        gun.name = "M4A1";
        gun.gunId = "tacz:m4a1";
        root.guns.add(gun);

        AttachmentJson scope = new AttachmentJson();
        scope.key = "armory:red_dot";
        scope.name = "红点瞄具";
        scope.type = "SCOPE";
        scope.attachmentId = "tacz:red_dot";
        root.attachments.add(scope);

        AttachmentJson muzzle = new AttachmentJson();
        muzzle.key = "armory:suppressor";
        muzzle.name = "消音器";
        muzzle.type = "MUZZLE";
        muzzle.attachmentId = "tacz:suppressor";
        root.attachments.add(muzzle);

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
        }
    }

    private static ResourceLocation parseRl(String raw, String fieldName, List<String> warnings) {
        if (raw == null || raw.isBlank()) {
            warnings.add("空资源定位符: " + fieldName);
            return null;
        }
        ResourceLocation rl = ResourceLocation.tryParse(raw);
        if (rl == null) {
            warnings.add("非法资源定位符 [" + fieldName + "]: " + raw);
        }
        return rl;
    }

    private static AttachmentType parseAttachmentType(String raw, List<String> warnings) {
        if (raw == null || raw.isBlank()) {
            warnings.add("空配件类型");
            return null;
        }
        try {
            return AttachmentType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            warnings.add("非法配件类型: " + raw);
            return null;
        }
    }

    private static String safeText(String s) {
        return s == null ? "" : s.trim();
    }
}
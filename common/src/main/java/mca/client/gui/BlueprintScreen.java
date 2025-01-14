package mca.client.gui;

import mca.client.gui.widget.RectangleWidget;
import mca.cobalt.network.NetworkHandler;
import mca.entity.ai.Rank;
import mca.network.GetVillageRequest;
import mca.network.ReportBuildingMessage;
import mca.network.SaveVillageMessage;
import mca.resources.API;
import mca.resources.data.BuildingType;
import mca.server.world.data.Building;
import mca.server.world.data.BuildingTasks;
import mca.server.world.data.Village;
import mca.util.compat.RenderSystemCompat;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BlueprintScreen extends Screen {
    //gui element Y positions
    private final int positionTaxes = -60;
    private final int positionBirth = -10;
    private final int positionMarriage = 40;
    private final int rankTaxes = 100;
    private final int rankBirth = 1;
    private final int rankMarriage = 1;
    private final int fromCenter = 150;
    private Village village;
    private int reputation;
    private boolean showCatalog;
    private ButtonWidget[] buttonTaxes;
    private ButtonWidget[] buttonBirths;
    private ButtonWidget[] buttonMarriage;
    private List<ButtonWidget> catalogButtons = new LinkedList<>();

    private static final Identifier ICON_TEXTURES = new Identifier("mca:textures/buildings.png");
    private BuildingType selectedBuilding;

    public BlueprintScreen() {
        super(new LiteralText("Blueprint"));
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void saveVillage() {
        NetworkHandler.sendToServer(new SaveVillageMessage(village));
    }

    private void changeTaxes(int d) {
        village.setTaxes(Math.max(0, Math.min(100, village.getTaxes() + d)));
        saveVillage();
    }

    private void changePopulationThreshold(int d) {
        village.setPopulationThreshold(Math.max(0, Math.min(100, village.getPopulationThreshold() + d)));
        saveVillage();
    }

    private void changeMarriageThreshold(int d) {
        village.setMarriageThreshold(Math.max(0, Math.min(100, village.getMarriageThreshold() + d)));
        saveVillage();
    }

    private ButtonWidget[] createValueChanger(int x, int y, int w, int h, Consumer<Boolean> onPress) {
        ButtonWidget[] buttons = new ButtonWidget[3];

        buttons[1] = addButton(new ButtonWidget(x - w / 2, y, w / 4, h,
                new LiteralText("<<"), (b) -> onPress.accept(false)));

        buttons[2] = addButton(new ButtonWidget(x + w / 4, y, w / 4, h,
                new LiteralText(">>"), (b) -> onPress.accept(true)));

        buttons[0] = addButton(new ButtonWidget(x - w / 4, y, w / 2, h,
                new LiteralText(""), (b) -> {
        }));

        return buttons;
    }

    protected void drawBuildingIcon(MatrixStack transform, int x, int y, int u, int v) {
        transform.push();
        transform.translate(x - 7, y - 7, 0);
        transform.scale(0.66f, 0.66f, 0.66f);
        this.drawTexture(transform, 0, 0, u, v, 20, 20);
        transform.pop();
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetVillageRequest());

        addButton(new ButtonWidget(width / 2 - 66, height / 2 + 80, 64, 20, new LiteralText("Exit"), (b) -> MinecraftClient.getInstance().openScreen(null)));
        addButton(new ButtonWidget(width / 2 + 2, height / 2 + 80, 64, 20, new LiteralText("Add Building"), (b) -> {
            NetworkHandler.sendToServer(new ReportBuildingMessage());
            NetworkHandler.sendToServer(new GetVillageRequest());
        }));
        addButton(new ButtonWidget(width / 2 - fromCenter - 32, height / 2 + 80, 64, 20, new LiteralText("Catalog"), (b) -> {
            toggleButtons(buttonTaxes, false);
            toggleButtons(buttonBirths, false);
            toggleButtons(buttonMarriage, false);
            setCatalogOpen(!showCatalog);
        }));

        //taxes
        buttonTaxes = createValueChanger(width / 2 + fromCenter, height / 2 + positionTaxes + 10, 80, 20, (b) -> changeTaxes(b ? 10 : -10));
        toggleButtons(buttonTaxes, false);

        //birth threshold
        buttonBirths = createValueChanger(width / 2 + fromCenter, height / 2 + positionBirth + 10, 80, 20, (b) -> changePopulationThreshold(b ? 10 : -10));
        toggleButtons(buttonBirths, false);

        //marriage threshold
        buttonMarriage = createValueChanger(width / 2 + fromCenter, height / 2 + positionMarriage + 10, 80, 20, (b) -> changeMarriageThreshold(b ? 10 : -10));
        toggleButtons(buttonMarriage, false);

        //list catalog button
        int row = 0;
        int col = 0;
        int size = 21;
        int x = width / 2 - 5 * size - 32;
        int y = (int)(height / 2 - 2.5 * size);
        catalogButtons.clear();
        for (BuildingType bt : API.getVillagePool()) {
            if (bt.visible()) {
                TexturedButtonWidget widget = new TexturedButtonWidget(
                        row * size + x - 10, col * size + y - 10, 20, 20, bt.iconU(), bt.iconV() + 20, 20, ICON_TEXTURES, 256, 256, button -> {
                    selectBuilding(bt);
                    button.active = false;
                    catalogButtons.forEach(b -> b.active = true);
                }, new TranslatableText("buildingType." + bt.name()));
                catalogButtons.add(addButton(widget));

                row++;
                if (row > 5) {
                    row = 0;
                    col++;
                }
            }
        }

        setCatalogOpen(false);
    }

    private void setCatalogOpen(boolean open) {
        showCatalog = open;
        catalogButtons.forEach(b -> b.active = open);
        catalogButtons.forEach(b -> b.visible = open);
    }

    private void selectBuilding(BuildingType b) {
        selectedBuilding = b;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack transform, int sizeX, int sizeY, float offset) {
        renderBackground(transform);

        if (showCatalog) {
            renderCatalog(transform);
        } else if (village != null && client != null) {
            //name
            transform.push();
            transform.scale(2.0f, 2.0f, 2.0f);
            drawCenteredText(transform, textRenderer, village.getName(), width / 4, height / 4 - 55, 0xffffffff);
            transform.pop();

            //population
            drawCenteredText(transform, textRenderer, "Buildings: " + village.getBuildings().size(), width / 2, height / 2 - 90, 0xffffffff);
            drawCenteredText(transform, textRenderer, "Population: " + village.getPopulation() + " of " + village.getMaxPopulation(), width / 2, height / 2 - 80, 0xffffffff);

            //update text
            buttonTaxes[0].setMessage(new LiteralText(village.getTaxes() + "%"));
            buttonBirths[0].setMessage(new LiteralText(village.getPopulationThreshold() + "%"));
            buttonMarriage[0].setMessage(new LiteralText(village.getMarriageThreshold() + "%"));

            //rank
            Rank rank = village.getTasks().getRank(reputation);
            Text rankStr = new TranslatableText("gui.village.rank." + rank.ordinal());
            int rankColor = rank.ordinal() == 0 ? 0xffff0000 : 0xffffff00;
            drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.rank", rankStr), width / 2 - fromCenter, height / 2 - 50 - 15, rankColor);
            drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.reputation", String.valueOf(reputation)), width / 2 - fromCenter, height / 2 - 50, rank.ordinal() == 0 ? 0xffff0000 : 0xffffffff);

            //tasks
            Text str = new TranslatableText("task.reputation", String.valueOf(rank.getReputation()));
            drawCenteredText(transform, textRenderer, str, width / 2 - fromCenter, height / 2 - 22, reputation >= rank.getReputation() ? 0xff00ff00 : 0xffff0000);

            int i = 0;
            for (String name : BuildingTasks.NAMES) {
                Text task = new TranslatableText("task." + name);
                drawCenteredText(transform, textRenderer, task, width / 2 - fromCenter, height / 2 - 10 + i * 12, village.getTasks().isCompleted(name) ? 0xff00ff00 : 0xffdddddd);
                i++;
            }

            //taxes
            drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.taxes"), width / 2 + fromCenter, height / 2 + positionTaxes, 0xffffffff);
            if (rank.ordinal() < rankTaxes) {
                drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.taxesNotImplemented"), width / 2 + fromCenter, height / 2 + positionTaxes + 15, 0xffffffff);
                toggleButtons(buttonTaxes, false);
            } else {
                toggleButtons(buttonTaxes, true);
            }

            drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.birth"), width / 2 + fromCenter, height / 2 + positionBirth, 0xffffffff);
            if (rank.ordinal() < rankBirth) {
                drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.rankTooLow"), width / 2 + fromCenter, height / 2 + positionBirth + 15, 0xffffffff);
                toggleButtons(buttonBirths, false);
            } else {
                toggleButtons(buttonBirths, true);
            }

            drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.marriage"), width / 2 + fromCenter, height / 2 + positionMarriage, 0xffffffff);
            if (rank.ordinal() < rankMarriage) {
                drawCenteredText(transform, textRenderer, new TranslatableText("gui.village.rankTooLow"), width / 2 + fromCenter, height / 2 + positionMarriage + 15, 0xffffffff);
                toggleButtons(buttonMarriage, false);
            } else {
                toggleButtons(buttonMarriage, true);
            }

            //map
            renderMap(transform);
        }

        super.render(transform, sizeX, sizeY, offset);
    }

    private void renderMap(MatrixStack transform) {
        int mapSize = 70;
        RectangleWidget.drawRectangle(transform, width / 2 - mapSize, height / 2 - mapSize, width / 2 + mapSize, height / 2 + mapSize, 0xffffff88);

        transform.push();

        RenderSystemCompat.setShaderTexture(0, ICON_TEXTURES);

        //center and scale the map
        float sc = (float)mapSize / village.getSize();
        transform.translate(width / 2.0, height / 2.0, 0);
        transform.scale(sc, sc, 0.0f);
        transform.translate(-village.getCenter().getX(), -village.getCenter().getZ(), 0);

        //show the players location
        ClientPlayerEntity player = client.player;
        if (player != null) {
            RectangleWidget.drawRectangle(transform, (int)player.getX() - 1, (int)player.getZ() - 1, (int)player.getX() + 1, (int)player.getZ() + 1, 0xffff00ff);
        }

        int mouseRawX = (int)(client.mouse.getX() * width / client.getWindow().getFramebufferWidth());
        int mouseRawY = (int)(client.mouse.getY() * height / client.getWindow().getFramebufferHeight());
        int mouseX = (int)((mouseRawX - width / 2.0) / sc + village.getCenter().getX());
        int mouseY = (int)((mouseRawY - height / 2.0) / sc + village.getCenter().getZ());

        //buildings
        Building hoverBuilding = null;
        for (Building building : village.getBuildings().values()) {
            BuildingType bt = API.getVillagePool().getBuildingType(building.getType());

            if (bt.isIcon()) {
                BlockPos c = building.getCenter();
                drawBuildingIcon(transform, c.getX(), c.getZ(), bt.iconU(), bt.iconV());

                //tooltip
                int margin = 6;
                if (c.getSquaredDistance(new Vec3i(mouseX, c.getY(), mouseY)) < margin * margin) {
                    hoverBuilding = building;
                }
            } else {
                BlockPos p0 = building.getPos0();
                BlockPos p1 = building.getPos1();
                RectangleWidget.drawRectangle(transform, p0.getX(), p0.getZ(), p1.getX(), p1.getZ(), bt.getColor());

                //tooltip
                int margin = 2;
                if (mouseX >= p0.getX() - margin && mouseX <= p1.getX() + margin && mouseY >= p0.getZ() - margin && mouseY <= p1.getZ() + margin) {
                    hoverBuilding = building;
                }
            }
        }

        transform.pop();

        if (hoverBuilding != null) {
            List<Text> lines = new LinkedList<>();

            //name
            BuildingType bt = API.getVillagePool().getBuildingType(hoverBuilding.getType());
            lines.add(new TranslatableText("buildingType." + bt.name()));
            lines.add(new TranslatableText("gui.building.size", String.valueOf(hoverBuilding.getSize())));

            //residents
            for (String name : hoverBuilding.getResidents().values()) {
                lines.add(new LiteralText(name));
            }

            //pois
            if (hoverBuilding.getPois().size() > 0) {
                lines.add(new LiteralText(hoverBuilding.getPois().size() + " pois"));
            }

            //present blocks
            for (Map.Entry<String, Integer> block : hoverBuilding.getBlocks().entrySet()) {
                lines.add(new LiteralText(block.getValue() + " x ").append(getBlockName(block.getKey())));
            }

            //render
            renderTooltip(transform, lines, mouseRawX, mouseRawY);
        }
    }

    private void renderCatalog(MatrixStack transform) {
        //title
        transform.push();
        transform.scale(2.0f, 2.0f, 2.0f);
        drawCenteredText(transform, textRenderer, "Building Catalog", width / 4, height / 4 - 55, 0xffffffff);
        transform.pop();

        //explanation
        drawCenteredText(transform, textRenderer, "Build special buildings by fulfilling those conditions", width / 2, height / 2 - 90, 0xffffffff);
        drawCenteredText(transform, textRenderer, "Work in Progress - you may build them but they have no effect yet", width / 2, height / 2 - 80, 0xffffffff);

        //building
        int w = 150;
        int x = width / 2 + 16 + w / 2;
        int y = height / 2 - 40;
        if (selectedBuilding != null) {
            drawCenteredText(transform, textRenderer, new TranslatableText("buildingType." + selectedBuilding.name()), x, y, selectedBuilding.getColor());
            drawCenteredText(transform, textRenderer, new TranslatableText("buildingType." + selectedBuilding.name() + ".description"), x, y + 12, 0xffffffff);

            //size
            Text size = selectedBuilding.size() == 0 ? new TranslatableText("gui.building.anySize") : new TranslatableText("gui.building.size", String.valueOf(selectedBuilding.size()));
            drawCenteredText(transform, textRenderer, size, x, y + 36, 0xffdddddd);

            //required blocks
            int i = 0;
            for (Map.Entry<String, Integer> b : selectedBuilding.blocks().entrySet()) {
                i++;
                drawCenteredText(transform, textRenderer, new LiteralText(b.getValue() + " x ").append(getBlockName(b.getKey())), x, y + 36 + 12 * i, 0xffffffff);
            }
        }
    }

    private Text getBlockName(String key) {
        //dis some hacking, no time to fix tho
        // TODO: This needs to be fixed on the backend
        Identifier id = new Identifier(key);
        if ("bed".equals(key)) {
            return Blocks.RED_BED.getName();
        }
        return new TranslatableText("block." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
    }

    private void toggleButtons(ButtonWidget[] buttons, boolean active) {
        for (ButtonWidget b : buttons) {
            b.active = active;
            b.visible = active;
        }
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }
}

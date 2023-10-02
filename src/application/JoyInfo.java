package application;

import gameutil.GameTools;
import gui.util.CanvasUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import joystick.JInputEX;
import joystick.JInputEXComponent;
import net.java.games.input.Component.POV;
import util.Misc;

public class JoyInfo {

	private JInputEX joystick;
	private Group root;
	private Stage mainStage;
	private Scene mainScene;
	private Canvas drawCanvas;
	private Canvas displayCanvas;
	private GraphicsContext mainGC;
	private int bps;
	private int bps2;
	private int maxBps;
	private long bpsCTime;
	private boolean resized;
	private boolean close;
	
	public JoyInfo(Stage stage, JInputEX joystick) {
		this.joystick = joystick;
		loadJoystickEvents();
		for (JInputEXComponent comp : joystick.getAxes())
			comp.setDeadZone(0.5f);
		for (JInputEXComponent comp : joystick.getTriggers())
			comp.setDeadZone(0.5f);
		close = false;
		bps = 0;
		bps2 = 0;
		maxBps = 0;
		bpsCTime = System.currentTimeMillis();
		mainStage = stage;
		root = new Group();
		drawCanvas = new Canvas(0,0);
		displayCanvas = new Canvas(0,0);
		drawCanvas.getGraphicsContext2D().setEffect(new BoxBlur(1, 1, 0));
		displayCanvas.getGraphicsContext2D().setEffect(new BoxBlur(1, 1, 0));
		root.getChildren().add(displayCanvas);
		mainGC = drawCanvas.getGraphicsContext2D();
		mainScene = new Scene(root);
		mainStage.setTitle("Joystick Tester");
		mainStage.setScene(mainScene);
		mainStage.setResizable(false);
		mainStage.setScene(mainScene);
		mainStage.setTitle("Joypad tester (" + joystick.getName() + ")");
		mainScene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.SPACE)
				Main.reallign();
			if (event.getCode() == KeyCode.ESCAPE)
				Main.close();
			if (event.getCode() == KeyCode.SUBTRACT || event.getCode() == KeyCode.MINUS)
				Main.decreaseZoom();
			if (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.ADD)
				Main.increaseZoom();
				
		});
		mainStage.setScene(mainScene);
		mainStage.show();
		mainLoop();
	}
	
	public void setResized (boolean value) {
		resized = value;
		mainLoop();
	}
	
	private void loadJoystickEvents() {
		joystick.setOnPovChangesEvent((j, c) ->
			{ if (c.getValue() != POV.CENTER) bps2++; });
		joystick.setOnAxisChangesEvent((j, c) -> bps2++);
		joystick.setOnTriggerChangesEvent((j, c) -> bps2++);
		joystick.setOnPressButtonEvent((j, c) -> bps2++);
	}

	private void drawText(String text, Color color, float x, float y) {
		double w = mainGC.getLineWidth();
		Paint c = mainGC.getStroke();
		mainGC.setLineWidth(1);
		mainGC.setStroke(color);
		mainGC.strokeText(text, x, y);
		mainGC.setLineWidth(w);
		mainGC.setStroke(c);
	}

	private void mainLoop() {
		mainGC.setFill(Color.CORNFLOWERBLUE);
		mainGC.fillRect(0,  0, (int)drawCanvas.getWidth(), (int)drawCanvas.getHeight());
		mainGC.setLineWidth(3);
		int x = 30, y = 50;
		drawText("Iteractions per sec: " + bps + "\t\tIteractions per sec (MAX): " + maxBps, Color.BLACK, x, 20);
		for (int n = 0; n < joystick.getTotalPovs(); n++, x += 134) {
			JInputEXComponent pov = joystick.getPov(n);
			mainGC.setFill(pov.getValue() >= POV.UP_LEFT && pov.getValue() <= POV.UP_RIGHT ? Color.GREEN : Color.BLACK);
		  mainGC.fillRect(x + 47, y + 10, 30, 30);
			mainGC.setFill(pov.getValue() >= POV.UP_RIGHT && pov.getValue() <= POV.DOWN_RIGHT ? Color.GREEN : Color.BLACK);
		  mainGC.fillRect(x + 77, y + 40, 30, 30);
			mainGC.setFill(pov.getValue() >= POV.DOWN_RIGHT && pov.getValue() <= POV.DOWN_LEFT ? Color.GREEN : Color.BLACK);
		  mainGC.fillRect(x + 47, y + 70, 30, 30);
			mainGC.setFill(pov.getValue() >= POV.DOWN_LEFT || pov.getValue() == POV.UP_LEFT ? Color.GREEN : Color.BLACK);
		  mainGC.fillRect(x + 17, y + 40, 30, 30);
		}
		for (int n = 0; n < joystick.getTotalAxes(); n += 2, x += 134) {
			JInputEXComponent axisX = joystick.getAxis(n);
			JInputEXComponent axisY = joystick.getAxis(n + 1);
			mainGC.setStroke(Color.BLACK);
			mainGC.strokeRect(x, y, 124, 124); 
			mainGC.setStroke(Color.LIGHTGRAY);
			mainGC.strokeOval(x + 62 - 42 * axisX.getDeadZone(), y + 62 - 42 * axisX.getDeadZone(),
												84 * axisX.getDeadZone(), 84 * axisX.getDeadZone()); 
			mainGC.setFill(axisX.isOnDeadZone() && axisY.isOnDeadZone() ? Color.RED : Color.GREEN);
			mainGC.fillOval(x + 42 + axisX.getValue() * 40, y + 42 + axisY.getValue() * 40, 40, 40);
			mainGC.setStroke(Color.YELLOWGREEN);
			mainGC.strokeOval(x + 62 + axisX.getValue() * 40, y + 62 + axisY.getValue() * 40, 1, 1);
			drawText(String.format("X: %.3f \t Y: %.3f", axisX.getValue(), axisY.getValue()), Color.BLACK, x + 10, y + 140);
		}
		for (int n = 0; n < joystick.getTotalTriggers(); n++, y += 55) {
			JInputEXComponent trigger = joystick.getTrigger(n);
			mainGC.setFill(trigger.isOnDeadZone() ? Color.RED : Color.GREEN);
			mainGC.fillRect(x + 2, y, 74 * (1 + trigger.getValue()), 25); 
			mainGC.setStroke(Color.BLACK);
			mainGC.strokeRect(x, y, 150, 25); 
			drawText(String.format("X: %.3f", trigger.getValue()), Color.BLACK, x + 10, y + 40);
		}
		int maxX = x + 190;
		x = 15; y += 134;
		for (int n = 0; n < joystick.getTotalButtons(); n++, x += 60) {
			if (x + 40 >= maxX - 20)
				{ x = 15; y += 60; }
			JInputEXComponent button = joystick.getButton(n);
			mainGC.setFill(button.isHold() ? Color.GREEN : Color.RED);
			mainGC.fillOval(x, y, 40, 40); 
		}
		x = 15; y += 90;
		drawText("All buttons, axes, triggers and pov as single buttons", Color.BLACK, 15, y - 15);
		for (int n = 0; n < joystick.getTotalComponents(); n++, x += 60) {
			if (x + 40 >= maxX - 20)
				{ x = 15; y += 60; }
			JInputEXComponent component = joystick.getComponent(n);
			mainGC.setFill(!component.isOnDeadZone() ? Color.GREEN : Color.RED);
			mainGC.fillOval(x, y, 40, 40); 
		}
		if (!resized) {
			resized = true;
			drawCanvas.setWidth(maxX);
			drawCanvas.setHeight(y + 90);
			displayCanvas.setWidth(drawCanvas.getWidth() * Main.getZoom());
			displayCanvas.setHeight(drawCanvas.getHeight() * Main.getZoom());
			mainStage.setWidth(drawCanvas.getWidth() * Main.getZoom() + 10);
			mainStage.setHeight(drawCanvas.getHeight() * Main.getZoom() + 20);
		}
		CanvasUtils.copyCanvas(drawCanvas, displayCanvas);
		if (System.currentTimeMillis() >= bpsCTime) {
			bps = bps2;
			if (bps > maxBps)
				maxBps = bps;
			bps2 = 0;
			bpsCTime = System.currentTimeMillis() + 1000;
		}
		
	 	Misc.sleep(1);
	 	if (!close)
	 		GameTools.callMethodAgain(e -> mainLoop());
	 	else
	 		mainStage.close();
	}
	
	public void setWindowsPos(int x, int y) {
		mainStage.setX(x);
		mainStage.setY(y);
	}
	
	public Stage getMainStage()
		{ return mainStage; }

	public void close() {
		joystick.close();
		close = true;
	}
	
}
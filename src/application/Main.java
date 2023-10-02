package application;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import joystick.JInputEX;

public class Main extends Application {
	
	private static List<JoyInfo> joyInfos = new ArrayList<>();
	private static float zoom = 0.5f;
	private static int maxWindowWidth = 0;
	private static int maxWindowHeight = 0;
	private static boolean sideBySide = false;

	@Override
	public void start(Stage stage) {
		JInputEX.initializeJoysticks();
		JInputEX.setOnJoystickConnectedEvent(j -> {
			System.out.println(j.getName() + " was connected");
		});
		JInputEX.setOnJoystickDisconnectedEvent(j -> {
			System.out.println(j.getName() + " was disconnected");
		});
		for (JInputEX joy : JInputEX.getJoysticks())
			joyInfos.add(new JoyInfo(new Stage(), joy));
		setMaxWindowSize();
	}
	
	private static void setMaxWindowSize() {
		maxWindowWidth = 0;
		maxWindowHeight = 0;
		for (JoyInfo ji : joyInfos) {
			if (ji.getMainStage().getWidth() > maxWindowWidth)
				maxWindowWidth = (int)ji.getMainStage().getWidth();
			if (ji.getMainStage().getHeight() > maxWindowHeight)
				maxWindowHeight = (int)ji.getMainStage().getHeight();
		}
	}
	
	public static void reallign() {
		int x = 0, y = 0;
		for (JoyInfo j : joyInfos) {
			j.setWindowsPos(x, y);
			if (sideBySide && (x += maxWindowWidth) + maxWindowWidth > 1920)
				{ x = 0; y += maxWindowHeight; }
		}
		sideBySide = !sideBySide;
	}
	
	public static void close() {
		for (JoyInfo j : joyInfos)
			j.close();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public static double getZoom()
		{ return zoom; }

	public static void decreaseZoom() {
		if (zoom > 0.25f) {
			zoom /= 2;
			for (JoyInfo j : joyInfos)
				j.setResized(false);
			setMaxWindowSize();
		}
	}

	public static void increaseZoom() {
		zoom *= 2;
		for (JoyInfo j : joyInfos)
			j.setResized(false);
		setMaxWindowSize();
	}

}
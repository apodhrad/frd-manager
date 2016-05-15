package frdext;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

public class FrdManager {

	public static final int START_TIMEOUT = 60 * 1000;
	public static final String FRD_PATH = "/home/apodhrad/Software/FreeRapid-0.9u4";

	private static FrdManager INSTANCE;

	private FrdRunner frdRunner;

	public static FrdManager getInstance() {
		if (INSTANCE == null) {
			File jarFile = new File(FRD_PATH, "frd.jar");

			// Set class loader
			URL url = null;
			try {
				url = jarFile.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new FrdException(e);
			}
			ClassLoader systemClassLoader = Thread.currentThread().getContextClassLoader();
			ClassLoader izPackClassLoader = new URLClassLoader(new URL[] { url }, systemClassLoader);
			Thread.currentThread().setContextClassLoader(izPackClassLoader);
			INSTANCE = new FrdManager(new FrdRunner(jarFile, izPackClassLoader));
		}
		return INSTANCE;
	}

	private FrdManager(FrdRunner frdRunner) {
		this.frdRunner = frdRunner;
	}

	private void checkAndStart() {
		if (!frdRunner.hasStarted()) {
			frdRunner.start();
			int time = 0;
			while (!frdRunner.hasStarted() || time > START_TIMEOUT) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				time += 500;
			}
			if (time > START_TIMEOUT) {
				throw new FrdException("Frd didn't start in " + START_TIMEOUT + "ms");
			}
		}
	}

	public static void addUrl(String url) {
		getInstance().checkAndStart();

		new JButtonOperator(new JFrameOperator(FrdLabels.MAIN_TITLE), "Add URL(s)").push();
		new JEditorPaneOperator(new JDialogOperator(FrdLabels.NEW_URL_TITLE)).setText(url);
		new JButtonOperator(new JDialogOperator(FrdLabels.NEW_URL_TITLE), "Start").push();
	}

	public static List<FrdData> getData() {
		getInstance().checkAndStart();

		List<FrdData> data = new ArrayList<FrdData>();
		new JTableOperator(new JFrameOperator(FrdLabels.MAIN_TITLE)).getProperties();
		TableModel model = new JTableOperator(new JFrameOperator(FrdLabels.MAIN_TITLE)).getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			Object downloadFile = model.getValueAt(i, 1);
			FrdData frdData = new FrdData();
			frdData.setName(getString(downloadFile, "fileName"));
			frdData.setSize(getInteger(downloadFile, "fileSize"));
			frdData.setDownloaded(getInteger(downloadFile, "downloaded"));
			frdData.setState(getString(downloadFile, "state"));
			frdData.setSpeed(getDouble(downloadFile, "speed"));
			frdData.setAverageSpeed(getDouble(downloadFile, "averageSpeed"));
			data.add(frdData);
		}
		return data;
	}

	private static String getString(Object obj, String key) {
		StringBuffer methodName = new StringBuffer("get");
		methodName.append(String.valueOf(key.charAt(0)).toUpperCase());
		methodName.append(key.substring(1));
		try {
			Method m = obj.getClass().getDeclaredMethod(methodName.toString());
			Object result = m.invoke(obj);
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int getInteger(Object obj, String key) {
		return Integer.valueOf(getString(obj, key));
	}

	private static double getDouble(Object obj, String key) {
		return Double.valueOf(getString(obj, key));
	}

}

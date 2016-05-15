package frdext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: save the standard and error outputs to the log directory
 */
public class FrdRunner extends Thread {

	private Logger log = LoggerFactory.getLogger(FrdRunner.class);

	private File jarFile;
	private Throwable throwable = null;
	private volatile int status = Integer.MIN_VALUE;
	private volatile boolean started = false;

	private SecurityManager systemSecurityManager = System.getSecurityManager();
	private ClassLoader systemClassLoader = Thread.currentThread().getContextClassLoader();

	private ClassLoader izPackClassLoader;

	public FrdRunner(File jarFile, ClassLoader izPackClassLoader) {
		super(jarFile.getName());

		this.jarFile = jarFile;
		this.izPackClassLoader = izPackClassLoader;

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			public void uncaughtException(Thread thread, Throwable throwable) {
				FrdRunner.this.throwable = throwable;
			}

		});
	}

	@Override
	public void run() {
		try {
			System.setSecurityManager(new SecurityManagerImpl(this));

			Class<?> clazz = izPackClassLoader.loadClass(getMainClass());
			Method method = clazz.getMethod("main", new Class[] { String[].class });
			List<String> mainArgs = new ArrayList<String>();

			method.invoke(null, new Object[] { mainArgs.toArray(new String[mainArgs.size()]) });

			new JFrameOperator(FrdLabels.MAIN_TITLE);
			
			started = true;

			while (status == Integer.MIN_VALUE) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					log.warn("Thread was stopped by force");
				}
			}
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
			throw new FrdException(t);
		} finally {
			started = false;
			System.setSecurityManager(systemSecurityManager);
			Thread.currentThread().setContextClassLoader(systemClassLoader);
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
		// The thread is not running anymore
		started = false;
		// Dispose any dialog.
		JDialog jd = JDialogOperator.findJDialog("", false, false);
		if (jd != null) {
			jd.dispose();
		}
		// Dispose any frame.
		JFrame ui = JFrameOperator.findJFrame(" ", false, false);
		if (ui != null) {
			ui.dispose();
		}

		System.setSecurityManager(systemSecurityManager);
		Thread.currentThread().setContextClassLoader(systemClassLoader);
	}

	public void end() {
		this.status = -5;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public int getStatus() {
		return status;
	}

	public boolean hasStarted() {
		return started;
	}

	private String getMainClass() throws IOException {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(this.jarFile);
			Manifest manifest = jarFile.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			return attributes.getValue("Main-Class");
		} finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}
	}

	private static class SecurityManagerImpl extends SecurityManager {

		private FrdRunner runner = null;

		private SecurityManagerImpl(FrdRunner runner) {
			this.runner = runner;
		}

		@Override
		public void checkExit(int status) {
			super.checkExit(status);
			runner.status = status;
			// No IZPack System.exit(int) call.
			throw new SecurityException("Caught IZPack exit call.");
		}

		@Override
		public void checkPermission(Permission perm) {
			// allow all
		}

		@Override
		public void checkPermission(Permission perm, Object context) {
			// allow all
		}

	}

}

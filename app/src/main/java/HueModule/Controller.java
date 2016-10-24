package HueModule;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.List;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

public class Controller {
	String username = "classAid-hue";
	//String username = "tBJQBBa2Rmjsrn8c";
	String lastIpAddress = "192.168.0.101";

	private PHHueSDK phHueSDK;
	private List<PHLight> allLights;
	private static final int MAX_HUE=65535;

	public Controller(){
        phHueSDK = PHHueSDK.getInstance();
	}

	public void findBridges() throws InterruptedException {
		//phHueSDK = PHHueSDK.getInstance();
		PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.search(true, true);
        Thread.sleep(1000);
	}

	private PHSDKListener listener = new PHSDKListener() {
        private List<PHAccessPoint> accessPointsList;
		@Override
		public void onAccessPointsFound(List<PHAccessPoint> accessPointsList) {
            Log.w("abc", "Access Points Found. " + accessPointsList.size());
            this.accessPointsList = accessPointsList;
            //TODO - catch this event and register the bridge
		}

		@Override
		public void onAuthenticationRequired(PHAccessPoint accessPoint) {
			// Start the Pushlink Authentication.
			phHueSDK.startPushlinkAuthentication(accessPoint);
		}

		@Override
		public void onBridgeConnected(PHBridge bridge) {
			phHueSDK.setSelectedBridge(bridge);
			phHueSDK.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);

		}

		@Override
		public void onCacheUpdated(List<Integer> arg0, PHBridge arg1) {
		}

		@Override
		public void onConnectionLost(PHAccessPoint arg0) {
		}

		@Override
		public void onConnectionResumed(PHBridge arg0) {
		}

		@Override
		public void onError(int code, final String message) {

			if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
				System.out.println("Bridge not responding");
			}
			else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
				System.out.println("pushlink not pressed");
			}
			else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
				System.out.println("pushlink authentication failed");
			}
			else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
				System.out.println("Bridge not found");
			}
		}

		@Override
		public void onParsingErrors(List<PHHueParsingError> parsingErrorsList) {
			for (PHHueParsingError parsingError: parsingErrorsList) {
				System.out.println("ParsingError : " + parsingError.getMessage());
			}
		}
	};

	public PHSDKListener getListener() {
		return listener;
	}

	public void setListener(PHSDKListener listener) {
		this.listener = listener;
	}

	public boolean connectToLastKnownAccessPoint() throws InterruptedException {
        Thread.sleep(1000);
		if (username==null || lastIpAddress == null) {
			System.out.println("Missing Last Username or Last IP.  Last known connection not found.");
			return false;
		}
		PHAccessPoint accessPoint = new PHAccessPoint();
		accessPoint.setIpAddress(lastIpAddress);
		accessPoint.setUsername(username);
		phHueSDK.connect(accessPoint);
		return true;
	}

	public void setToGreen(){
		try{
			Thread.sleep(1000); // delay is required for the bridge to initialize
		}
		catch (Exception e){
			;
		}
		PHBridge bridge = phHueSDK.getSelectedBridge();
		allLights = bridge.getResourceCache().getAllLights();
		int selectedIndex =3; // since this is the third available device in the list
		PHLightState lightState = new PHLightState();

		String lightIdentifer = allLights.get(selectedIndex).getIdentifier();
		float xy[] = PHUtilities.calculateXYFromRGB(Color.rgb(0,255,0), Color.rgb(0,255,0), Color.rgb(0,255,0), "LCT001");
		lightState.setX(xy[0]);
		lightState.setY(xy[1]);


		phHueSDK.getSelectedBridge().updateLightState(lightIdentifer, lightState, null);
	}

	public void setToRed(){
		try{
			Thread.sleep(1000); // delay is required for the bridge to initialize
		}
		catch (Exception e){
			;
		}
		PHBridge bridge = phHueSDK.getSelectedBridge();
		allLights = bridge.getResourceCache().getAllLights();
		int selectedIndex =3; // since this is the third available device in the list
		PHLightState lightState = new PHLightState();

		String lightIdentifer = allLights.get(selectedIndex).getIdentifier();

		float xy[] = PHUtilities.calculateXYFromRGB(Color.rgb(255,0,0), Color.rgb(255,0,0), Color.rgb(255,0,0), "LCT001");
		lightState.setX(xy[0]);
		lightState.setY(xy[1]);


		phHueSDK.getSelectedBridge().updateLightState(lightIdentifer, lightState, null);
	}
}

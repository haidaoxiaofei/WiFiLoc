package com.example.wifiloc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.example.interfaces.EstimationListener;
import com.example.structures.AreaPoint;
import com.example.utils.CustomHttpClient;
import com.example.utils.MyJsonResponse;
import com.example.utils.ScanResultBuffer;
import com.google.gson.Gson;

public class SlideWindowProc {
	public static final int WINDOW_SIZE = 5;
	public static final int SLIDE_SIZE = 2;
	private int count = 0;//ap count
	private EstimationListener listener;
	private Map<Long, Integer> macMap = new HashMap<Long, Integer>();
	private List<ArrayList<Integer>> signals = new ArrayList<ArrayList<Integer>>();
    String url = null;
    float medianSignals[] = null;
    long macList[] = null;
    public boolean isLocalizating = false;


	public SlideWindowProc(EstimationListener listener) {
		super();
		this.listener = listener;
	}

	public void produceResult(){
		while (estimateWindowResult()) {
			slideToNextWin();
		}
	}
	
	public boolean slideToNextWin(){
		if (ScanResultBuffer.strengthList.size() - SLIDE_SIZE <= 0) {
			return false;
		}
		for (int i = 0; i < SLIDE_SIZE; i++) {
			ScanResultBuffer.macList.remove(0);
			ScanResultBuffer.strengthList.remove(0);
		}
		
		return true;
	}
	
	public boolean estimateWindowResult(){
		if (ScanResultBuffer.strengthList.size() < WINDOW_SIZE) {
			return false;
		}
		
		reset();
		long[] macTmp;
		float[] strengthTmp;
		for (int i = 0; i < WINDOW_SIZE; i++) {
			macTmp = ScanResultBuffer.macList.get(i);
			strengthTmp = ScanResultBuffer.strengthList.get(i);
			for (int j = 0; j < macTmp.length; j++) {
				if (macMap.containsKey(macTmp[j])) {
					Integer index = (Integer) macMap.get(macTmp[j]);
					signals.get(index).add((int) strengthTmp[j]);
				} else {
					macMap.put(macTmp[j], count);
					signals.add(new ArrayList<Integer>());
					signals.get(count).add((int) strengthTmp[j]);
					count++;
				}
			}
		}
		
		//calculate median strength value for each AP
		medianSignals = new float[macMap.size()];
		macList = new long[macMap.size()];
		Set<Long> macs = macMap.keySet();
		for (Long mac : macs) {
			int index = macMap.get(mac);
			macList[index] = mac;
			List<Integer> tmp = signals.get(index);
			Collections.sort(tmp);
			if (tmp.size() > 0) {
				if (tmp.size() % 2 == 1) {
					medianSignals[index] = tmp.get(tmp.size() / 2);
				} else {
					medianSignals[index] = (float) ((tmp.get(tmp.size() / 2) + tmp.get((tmp.size() - 1) / 2)) / 2.0);
				}
			}
			tmp.clear();
		}

        if (!isLocalizating){
            new RemoteLocalizationTask().execute();
        }


		return true;
	}
	public void reset(){
		count = 0;
		signals.clear();
		macMap.clear();
	}

    public class RemoteLocalizationTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
//            Log.i("response1", "background");
            isLocalizating = true;
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < macList.length; i++){
                sb.append(macList[i]);
                sb.append("#");
            }
            sb.append(";");
            for (int i = 0; i < medianSignals.length; i++){
                sb.append(medianSignals[i]);
                sb.append("#");
            }

//            String s12 = "102536546816#102536546817#102536546819#102536546821#102536548193#102536549168#102536549169#102536549171#102536549173#102536549392#102536549395#102536549472#102536549473#102536549475#102536549477#126442728032#126442728033#126442728035#126442728037#126442728064#126442728069#135804124832#135804124833#135804124835#135804124837#136539001584#136539001585#136539001587#136539001589#147462591760#147462591872#147462591873#147462591875#147462591877#147462592016#147462592017#147462592019#147462592021#147462592304#147462592305#147462592307#147462592309#147462592737#147462592739#147462592741#147462598083#147462598576#147462598577#147462598579#147462598581#154957632512#154957632513#154957632515#154957632517#5337882142704#5337882142709#5337883823712#5337883823713#5337883823715#5337883823717#14128664336128#14128664336129#14128664336131#14128664336133#114927550790273#114927550790277#114927550791424#114927550791425#114927550791427#114927550791429#115156867388416#115156867388417#115156867388419#115156867388421#198000299465472#198000299465473#198000299465475#198000299465477#198000299466912#198000299466913#198000299466915#198000299466917#198000299469584#198000299469585#198000299469587#198000299469589#;-75.0#-83.0#-83.5#-83.0#-83.0#-78.0#-78.0#-79.0#-76.0#-90.0#-86.0#-81.0#-80.0#-81.0#-81.0#-61.5#-62.0#-62.5#-61.5#-87.0#-83.0#-75.0#-75.0#-76.0#-76.0#-80.0#-79.0#-79.0#-79.0#-62.0#-87.0#-86.0#-86.0#-88.0#-77.0#-77.0#-78.5#-78.0#-59.0#-59.0#-59.0#-59.0#-80.0#-79.0#-79.0#-88.0#-80.0#-86.0#-87.0#-87.5#-87.0#-88.0#-87.5#-87.0#-77.0#-90.0#-91.0#-90.0#-87.0#-90.0#-80.0#-81.0#-82.0#-81.0#-90.0#-84.0#-86.0#-73.0#-87.0#-86.5#-72.0#-72.0#-74.0#-73.0#-84.0#-84.0#-83.0#-82.0#-81.0#-85.0#-75.0#-82.0#-81.0#-82.0#-83.0#-81.0#";
           postParameters.add(new BasicNameValuePair("fp", sb.toString()));
//            postParameters.add(new BasicNameValuePair("fp", s12));

            String response = null;
            try {
//                Log.i("response1", url);
//                Log.i("ProcessTime", "1:" + System.currentTimeMillis());
                response = CustomHttpClient.executeHttpPost(url, postParameters);
//                Log.i("ProcessTime", "2:" + System.currentTimeMillis());
                String res = response.toString();
                res = res.replaceAll("\\s+", "");
                MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));
                String s = resp.getContent();
                if (resp.getType() == MyJsonResponse.TYPES_ENUM.SUCCESS)
                    return s;
                else
                    return null;



            } catch (Exception e) {
                return null;
            }

        }
        @Override
        protected void onPostExecute(final String content) {
            JSONObject jb = null;
            Gson gson = new Gson();
            final AreaPoint ap = gson.fromJson(content, AreaPoint.class);

            listener.updateEstimatedPosition(ap);


            isLocalizating = false;
        }
    }
}

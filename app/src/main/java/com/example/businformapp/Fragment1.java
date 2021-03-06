package com.example.businformapp;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.content.Intent;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Fragment1 extends Fragment {
    private SearchView mSearchView;
    private ListView mListView;
    private RouteNameAdapter routeNameAdapter;
    Bundle bundle;

    private String searvicePath = "http://openapi.gbis.go.kr/ws/rest/";
    private String serviceName = "busrouteservice";
    private String operation = "";
    private String serviceKey = "serviceKey=2LGrVBKRbUxVD5dXYkOPLb9Sar7XnzXiJ4REz2%2FS60MTHKOjsVBL7ZL6wKMrBomsdEVmDHmH9xW7J2hvtgllxA%3D%3D";
    private String areaId = "";
    private String params = "&keyword=";
    private String arr[];
    private String tags[];
    String endTag = "";

    ArrayList<HashMap<String, String>> mapArrayList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override // MainActivity에서의 onCreate 메소드는 Fragment에서는 onCreateView 메소드에 작성합니다.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment에서는 MainActivity에서의 setContentView(R.layout.xml파일명) 대신 inflater가 존재합니다.
        // inflater는 xml로 정의된 view (또는 menu_search 등)를 실제 객체화 시키는 용도
        // 또한 setContentView의 부재로 findViewById(R.id.id명)은 바로 사용할 수 없고 앞에 getView 를 붙여주거나 inflater된 View의 변수명을 붙여주도록 합니다.
        // findViewById 은 xml 레이아웃에 정의되어있는 뷰를 가져오는 메소드 (참조 : https://yongku.tistory.com/entry/안드로이드-스튜디오Android-Studio-findViewById )

        try {
            bundle = getArguments();
            String arg = bundle.getString("query");
            params += arg;
        }
        catch (NullPointerException e) {
            System.out.println("정보 없음");
        }
        arr = new String[]{searvicePath, serviceName, operation, serviceKey, params};
        tags = new String[]{"routeId", "routeName", "routeTypeCd", "routeTypeName", "districtCd", "regionName"}; // 요청 태그
        endTag = "busRouteList";

        try {
            mapArrayList = new GetApiData(arr, tags, endTag).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }


        View view = inflater.inflate(R.layout.fragment1, null);
        setListView(view);
        return view;
    }

    public void setListView(View view) {
        // Fragment에서는 .this 대신 getActivity().getApplicationContext() 을 사용합니다.
        RouteNameAdapter adapter = new RouteNameAdapter(getActivity().getApplicationContext(), mapArrayList);

        ListView listView = (ListView)view.findViewById(R.id.listView1);
        listView.setAdapter(adapter);

        // 클릭시 상세 정보가 나타남
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // RouteInfoActivity로 넘어감
                Intent intent = new Intent(getActivity().getApplicationContext(), RouteInfoActivity.class);
                // 클릭 위치 탐색
                HashMap<String, String> data = mapArrayList.get(position);

                JsonDataManager jsonManager = new JsonDataManager(requireContext());
                JSONArray jsArray = jsonManager.getData("Route");

                if (jsArray != null) {
                    Log.i("JSON", "Loaded: " + jsArray.toString());

                    boolean hasName = false;
                    for (int i = 0; i < jsArray.length(); i++) {
                        try {
                            JSONObject obj = jsArray.getJSONObject(i);
                            if (obj.get("routeId").equals(data.get("routeId"))) {
                                hasName = true;
                                break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!hasName) {
                        jsArray.put(new JSONObject(data));
                        jsonManager.setData(jsArray, "Route");
                    }
                }
                else {
                    jsArray = new JSONArray();
                    jsArray.put(new JSONObject(data));
                    jsonManager.setData(jsArray, "Route");
                }
                Log.i("JSON", "Saved: " + data.toString());

                // putExtra 첫 인자는 식별 태그, 두번째는 다음 엑티비티에 넘길 정보
                intent.putExtra("routeId", data.get("routeId"));
                intent.putExtra("routeName", data.get("routeName"));
                intent.putExtra("routeTypeName", data.get("routeTypeName"));
                intent.putExtra("districtCd", data.get("districtCd"));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    class RouteNameAdapter extends BaseAdapter {
        private Context applicationContext;
        private ArrayList<HashMap<String, String>> array_data;

        private ViewHolder mViewHolder;

        public RouteNameAdapter(Context applicationContext, ArrayList<HashMap<String, String>> mapArrayList) {
            this.applicationContext = applicationContext;
            this.array_data = mapArrayList;
        }

        @Override
        public int getCount() {
            return array_data.size();
        }

        @Override
        public Object getItem(int position) {
            return array_data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(applicationContext).inflate(R.layout.route_name_list_item, parent, false); // 리스트 뷰에 들어갈 아이템.xml 파일
                mViewHolder = new ViewHolder(convertView);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, String> data = array_data.get(position);
            System.out.println("------------------------------> "+data);
            mViewHolder.route_name_title.setText(data.get("routeName")); // 받아올 정보

            String idCode = data.get("routeTypeCd");
            String regionCode = data.get("districtCd");
            String regionName = data.get("regionName");

            TypeAndRegionCode typeAndRegionCode = new TypeAndRegionCode(idCode, regionCode);
            regionCode = typeAndRegionCode.getRegionName();
            idCode = typeAndRegionCode.getRouteType();

            mViewHolder.route_name_text.setText(regionName + "시" + " " + idCode);

            return convertView;
        }

        private class ViewHolder {
            private TextView route_name_title; // xml의 title id
            private TextView route_name_text; // // xml의 text id

            public ViewHolder(View convertView) {
                route_name_title = (TextView) convertView.findViewById(R.id.route_name_title);
                route_name_text = (TextView) convertView.findViewById(R.id.route_name_text);
            }
        }
    }
}
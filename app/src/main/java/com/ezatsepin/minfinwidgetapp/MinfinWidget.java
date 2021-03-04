package com.ezatsepin.minfinwidgetapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.androidplot.ui.Anchor;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.StepModel;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class MinfinWidget extends AppWidgetProvider {

    private static final String WIDGET_CLICKED = "WIDGET_CLICKED";
    private static final String ACTION_APPWIDGET_UPDATE = AppWidgetManager.ACTION_APPWIDGET_UPDATE;

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.minfin_widget);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {



        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.minfin_widget);
        ComponentName watchWidget = new ComponentName(context, MinfinWidget.class);

        remoteViews.setOnClickPendingIntent(R.id.widget_layout, getPendingSelfIntent(context, WIDGET_CLICKED));

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);



    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        final String action = intent.getAction();

        if (WIDGET_CLICKED.equals(action) || ACTION_APPWIDGET_UPDATE.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.minfin_widget);
            ComponentName watchWidget = new ComponentName(context, MinfinWidget.class);

            Integer height = 220,
                    width = 440;

            XYPlot plot = new XYPlot(context, "");
            plot.setDrawingCacheEnabled(true);

            plot.setBackgroundColor(Color.TRANSPARENT);
            plot.getGraph().getBackgroundPaint().setColor(Color.TRANSPARENT);
            plot.getGraph().getGridBackgroundPaint().setColor(Color.WHITE);
            plot.getGraph().setPadding(42, 25, 20, 20);
            plot.getGraph().setMargins(0, 0, 0, 0);

            Size sz = new Size(height, SizeMode.ABSOLUTE, width, SizeMode.ABSOLUTE);
            plot.getGraph().setSize(sz);
            plot.layout(0, 0, width, height);


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String str = "http://localhost/test3.php";
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                URL url = new URL(str);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                try {
                    JSONObject jsonObj = new JSONObject(stringBuffer.toString());
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    String avgTxt = decimalFormat.format(jsonObj.get("b")) + "   " + decimalFormat.format(jsonObj.get("s"));

                    String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()),
                           fullInfo = avgTxt + "   " + timeStamp;

                    plot.getTitle().position(18, HorizontalPositioning.ABSOLUTE_FROM_RIGHT, 3, VerticalPositioning.ABSOLUTE_FROM_TOP, Anchor.RIGHT_TOP);
                    plot.setTitle(fullInfo);

                    Integer origin = (Integer) jsonObj.get("o");

                    JSONArray data = jsonObj.getJSONArray("b_data");
                    Number[] seriesB = new Number[data.length()];
                    for (int i = 0; i < data.length(); ++i) {
                        seriesB[i] = data.optDouble(i);
                    }

                    JSONArray data2 = jsonObj.getJSONArray("s_data");
                    Number[] seriesS = new Number[data2.length()];
                    for (int i = 0; i < data2.length(); ++i) {
                        seriesS[i] = data2.optDouble(i);
                    }

                    JSONArray data3 = jsonObj.getJSONArray("d_data");
                    Integer[] seriesD = new Integer[data3.length()];
                    for (int i = 0; i < data3.length(); ++i) {
                        seriesD[i] = data3.optInt(i);
                    }


                    List<Number> ListB = Arrays.asList(seriesB);
                    List<Integer> ListD = Arrays.asList(seriesD);
                    XYSeries series1 = new SimpleXYSeries(ListD, ListB, "");
                    LineAndPointFormatter series1Format = new LineAndPointFormatter(context, R.xml.line_formatter_b);
                    plot.addSeries(series1, series1Format);

                    List<Number> ListS = Arrays.asList(seriesS);
                    XYSeries series2 = new SimpleXYSeries(ListD, ListS, "");
                    LineAndPointFormatter series2Format = new LineAndPointFormatter(context, R.xml.line_formatter_s);
                    plot.addSeries(series2, series2Format);


                    plot.getLegend().setHeight(0);
                    plot.getLegend().setVisible(false);

                    StepModel sm = new StepModel(StepMode.INCREMENT_BY_FIT, 86400);
                    plot.setUserDomainOrigin(origin);
                    plot.setDomainStepModel(sm);

                    XYGraphWidget.Edge[] edges = {XYGraphWidget.Edge.BOTTOM, XYGraphWidget.Edge.LEFT};

                    plot.getGraph().setLineLabelEdges(edges);
                    plot.getGraph().getLineLabelInsets().setBottom(-12);
                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setTextAlign(Paint.Align.LEFT);
                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setTextSize(13);
                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setColor(Color.WHITE);


                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
                        @Override
                        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                            java.util.Date time = new java.util.Date((long) ((Number) obj).intValue() * 1000);
                            return toAppendTo.append(time.getDate());
                        }

                        @Override
                        public Object parseObject(String source, ParsePosition pos) {
                            // unused
                            return null;
                        }
                    });


                    plot.getGraph().getLineLabelInsets().setLeft(-2);
                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setTextAlign(Paint.Align.RIGHT);
                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setTextSize(12);
                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setColor(Color.WHITE);
                    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("0.00"));


                    Arrays.sort(seriesS);
                    Arrays.sort(seriesB);

                    double minval = seriesB[0].doubleValue() - 0.01;
                    double maxval = seriesS[seriesS.length-1].doubleValue() + 0.01;
                    plot.setRangeBoundaries(minval, maxval, BoundaryMode.FIXED);


                    Bitmap bmp = plot.getDrawingCache();
                    remoteViews.setBitmap(R.id.plot, "setImageBitmap", bmp);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
            }
            finally
            {
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
            Toast.makeText(context, R.string.data_updated, Toast.LENGTH_SHORT).show();
        }
    }
}


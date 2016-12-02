package com.belladati.android.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.belladati.httpclientandroidlib.client.utils.URIBuilder;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.impl.BellaDatiServiceWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

/**
 * Created by KarimT on 29.11.2016.
 */
public class LineChart extends com.github.mikephil.charting.charts.LineChart  {

    public void setAxesTextColour(int axesTextColour) {
        this.axesTextColour = axesTextColour;
    }

    private int axesTextColour;

    public void setValueTextColor(int valueTextColor) {
        this.valueTextColor = valueTextColor;
    }

    private int valueTextColor;
    public void setIdChart(String idChart) {
        this.idChart = idChart;
    }

    private String idChart;


    public void setFilterNode(ObjectNode filterNode) {
        this.filterNode = filterNode;
    }

    private ObjectNode filterNode;

    public void setLegendColour(int legendColour) {
        this.legendColour = legendColour;
    }

    private int legendColour;

    public void setService(BellaDatiService service) {
        this.service = service;
        this.wrapper=new BellaDatiServiceWrapper(this.service);
    }

    private BellaDatiService service;
    private BellaDatiServiceWrapper wrapper;


    public LineChart(Context context) {
        super(context);
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void createChart() throws Exception {
        if(service==null)
        {
            throw new Exception("Service must be set up");
        }

        if(idChart==null)
        {
            throw new Exception("Detected no chart id");
        }
        URIBuilder builder = new URIBuilder("api/reports/views/"+idChart+"/chart");

        if(filterNode!=null)
        {
            ObjectNode drilldownNode = new ObjectMapper().createObjectNode();
            drilldownNode.put("drilldown", filterNode);

            builder.addParameter("filter", drilldownNode.toString());
        }

        JsonNode jn = wrapper.loadJson(builder.toString());

        JsonNode content = jn.findPath("content");
        JsonNode elements = content.findPath("elements").get(0).findPath("values");
        JsonNode x_axis = content.findPath("x_axis").findPath("labels").findPath("labels");

        String colour=content.findPath("elements").get(0).findPath("colour").asText();
        setupChart();

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < elements.size(); i++) {

            entries.add(new Entry(elements.get(i).findPath("value").floatValue(), i));
        }
        LineDataSet dataSet = new LineDataSet(entries,content.findPath("elements").get(0).findPath("text").asText());
        if(valueTextColor==0)
            valueTextColor= Color.WHITE;

        dataSet.setColor(rgb(colour));
        dataSet.setValueTextColor(valueTextColor);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCubic(true);


        List<String> h_lables = new ArrayList<>();

        for (int l = 0; l < x_axis.size(); l++) {
            h_lables.add(x_axis.get(l).asText());
        }
        String[] xAxis = new String[h_lables.size()];
        xAxis = h_lables.toArray(xAxis);

        LineData lineData = new LineData(xAxis,dataSet);
        setDescription("");
        setData(lineData);
        invalidate();
        Legend legend = getLegend();
        if(legendColour==0)
            legendColour= Color.WHITE;
        legend.setTextColor(legendColour);
        legend.setWordWrapEnabled(true);

    }
    private void setupChart( ) {
        if(axesTextColour==0)
            axesTextColour= Color.WHITE;
        setMaxVisibleValueCount(40);
        // scaling can now only be done on x- and y-axis separately
        setPinchZoom(false);

        setDrawGridBackground(false);

        setHighlightFullBarEnabled(false);

        YAxis leftAxis = getAxisLeft();
        leftAxis.setStartAtZero(true); // this replaces setStartAtZero(true)

        leftAxis.setTextColor(axesTextColour);

        getAxisRight().setEnabled(false);

        XAxis xLabels = getXAxis();

        xLabels.setTextColor(axesTextColour);
        xLabels.setPosition(XAxis.XAxisPosition.TOP);
    }
}

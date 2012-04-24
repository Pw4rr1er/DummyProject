package com.android.demo.trial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.maps.Overlay.Snappable;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

// import com.geodesic.android.universalIM.GoogleAnalytics.GoogleAnalytics;

public class GetQuotesTab extends Activity
{
    private String m_szYahooURL = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=";
    private String m_szYahooURL1 = "&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
    YahooSymbolList symbols;
    ListView quotesList;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.getquotesactivity );

        // GoogleAnalytics.trackPageView( this, GoogleAnalytics.FAVORITESTAB_PAGE );

        Button btnSearch = (Button) findViewById( R.id.btnSearch );
        btnSearch.setOnClickListener( new OnClickListener()
        {

            @Override
            public void onClick( View v )
            {
                // TODO Auto-generated method stub
                EditText editTextSearch = (EditText) findViewById( R.id.edittextSearch );
                String szSearchString = editTextSearch.getText().toString();
                szSearchString = szSearchString.replace( " ", "%20" );
                final String szQueryURL = m_szYahooURL + szSearchString + m_szYahooURL1;

                String szSymbol = "";
                String szName = "";
                String szExch = "";
                String szType = "";
                try
                {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    request.setURI( new URI( szQueryURL ) );
                    HttpResponse response = httpclient.execute( request );
                    String szResponse = inputStreamToString( response.getEntity().getContent() ).toString();
                    Log.d( "GetQuotesTab", "Response is :" + szResponse );
                    if( !szResponse.startsWith( "Error" ) )
                    {
                        szResponse = szResponse.substring( szResponse.indexOf( '(' ) + 1, szResponse.length() - 1 );

                        JSONObject mainObject = new JSONObject( szResponse );
                        JSONObject resultSet = mainObject.getJSONObject( "ResultSet" );
                        JSONArray result = resultSet.getJSONArray( "Result" );

                        if( result.length() > 0 )
                        {
                            symbols = new YahooSymbolList();
                            for( int i = 0; i < result.length(); ++i )
                            {
                                JSONObject item = result.getJSONObject( i );
                                szSymbol = ( item.getString( "symbol" ) );
                                szName = ( item.getString( "name" ) );
                                szExch = ( item.getString( "exch" ) );
                                szType = ( item.getString( "type" ) );

                                YahooSymbol obj = new YahooSymbol();
                                obj.setSymbol( szSymbol );
                                obj.setName( szName );
                                obj.setExch( szExch );
                                obj.setType( szType );
                                symbols.add( obj );

                                Log.d( "Parsing Data Response ", " *******  symbol: " + szSymbol );
                                Log.d( "Parsing Data Response ", " *******  name: " + szName );
                                Log.d( "Parsing Data Response ", " *******  exch: " + szExch );
                                Log.d( "Parsing Data Response ", " *******  type: " + szType );
                            }
                            handler.sendEmptyMessage( 0 );
                        }
                        else
                        {
                            Log.d( "Search-onClick", "No Results found. Try Again..." );
                        }
                    }

                }
                catch( Exception e )
                {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }

        } );

        quotesList = (ListView) findViewById( R.id.quotesList );
        quotesList.setOnItemClickListener( new OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView< ? > arg0, View view, int arg2, long arg3 )
            {
                // TODO Auto-generated method stub
                TextView txtName = (TextView) view.findViewById( R.id.name );
                TextView txtExch = (TextView) view.findViewById( R.id.exch );
                TextView txtSymbol = (TextView) view.findViewById( R.id.symbol );

                String szName = txtName.getText().toString();
                String szExch = txtExch.getText().toString();
                String szSymbol = txtSymbol.getText().toString();

                Log.d( "On ITEM CLICK ", " Name is : " + szName );
                Log.d( "On ITEM CLICK ", " Exchg is : " + szExch );
                Log.d( "On ITEM CLICK ", " Symbol is : " + szSymbol );

                String szGetQuoteURL = "http://finance.yahoo.com/d/quotes.csv?s=" + szSymbol + "&f=snd1l1yr";
                FetchQuote( szGetQuoteURL );
            }
        } );

    }

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        TextView emptyView = (TextView) findViewById( android.R.id.empty );
        if( quotesList != null )
            emptyView.setVisibility( View.GONE );
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage( Message msg )
        {
            setAdapter();
        }
    };

    private void FetchQuote( String szURL )
    {
        // TODO Auto-generated method stub
        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI( new URI( szURL ) );
            HttpResponse response = httpclient.execute( request );
            String szResponse = inputStreamToString( response.getEntity().getContent() ).toString();
            Log.d( "GetQuotesTab-FetchQuote", "Response is :" + szResponse );
        }
        catch( Exception e )
        {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private StringBuilder inputStreamToString( InputStream content ) throws IOException
    {
        // TODO Auto-generated method stub
        String line = "";
        StringBuilder total = new StringBuilder();

        BufferedReader rd = new BufferedReader( new InputStreamReader( content ) );
        while( ( line = rd.readLine() ) != null )
        {
            total.append( line );
        }
        return total;
    }

    private void setAdapter()
    {
        // TODO Auto-generated method stub
        StockListAdapter adapter = new StockListAdapter( GetQuotesTab.this, symbols );
        quotesList.setAdapter( adapter );
        TextView emptyView = (TextView) findViewById( android.R.id.empty );
        if( quotesList != null )
            emptyView.setVisibility( View.GONE );
    }

    public class StockListAdapter extends BaseAdapter
    {

        private LayoutInflater mInflater;
        YahooSymbolList symbols = new YahooSymbolList();

        public StockListAdapter( Context context, YahooSymbolList data )
        {
            mInflater = LayoutInflater.from( context );
            symbols = data;
        }

        public void setData( YahooSymbolList data )
        {
            symbols = data;
        }

        @Override
        public int getCount()
        {
            // TODO Auto-generated method stub
            return symbols.size();
        }

        @Override
        public Object getItem( int position )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId( int position )
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            // TODO Auto-generated method stub
            final ViewHolder holder;

            if( convertView == null )
            {
                convertView = mInflater.inflate( R.layout.quotestabrow, null );
                holder = new ViewHolder();
                holder.stockname = (TextView) convertView.findViewById( R.id.name );
                holder.exch = (TextView) convertView.findViewById( R.id.exch );
                holder.symbol = (TextView) convertView.findViewById( R.id.symbol );
                convertView.setTag( holder );
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            YahooSymbol item = symbols.get( position );
            holder.stockname.setText( item.getName() );
            holder.exch.setText( item.getExch() + "-" + item.getType() );
            holder.symbol.setText( item.getSymbol() );
            return convertView;
        }

        class ViewHolder
        {

            TextView stockname, exch, symbol;
        }
    }
}

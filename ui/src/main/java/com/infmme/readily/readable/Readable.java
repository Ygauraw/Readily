package com.infmme.readily.readable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.infmme.readily.Constants;
import com.infmme.readily.R;
import com.infmme.readily.database.DataBundle;
import com.infmme.readily.essential.TextParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by infm on 6/12/14. Enjoy ;)
 */
abstract public class Readable implements Serializable {

	public static final int TYPE_RAW = 0;
	public static final int TYPE_CLIPBOARD = 1;
	public static final int TYPE_FILE = 2;
	public static final int TYPE_TXT = 3;
	public static final int TYPE_EPUB = 4;
	public static final int TYPE_NET = 5;

	protected StringBuilder text;
	protected String header;
	protected Long seconds;
	protected String path;
	protected Integer position;
	protected Integer type;
	protected DataBundle rowData;
	protected Boolean processFailed;

	protected List<String> wordList;
	protected List<Integer> delayList;
	protected List<Integer> emphasisList;

	public Readable(){
		text = new StringBuilder();
		wordList = new ArrayList<String>();
		delayList = new ArrayList<Integer>();
		emphasisList = new ArrayList<Integer>();
		rowData = new DataBundle();
		processFailed = false;
	}

	public static Readable createReadable(Context context, Bundle bundle){
		Readable readable = null;
		if (bundle != null){
			readable = createReadable(
					bundle.getInt(Constants.EXTRA_TYPE, -1),
					bundle.getString(Intent.EXTRA_TEXT, context.getResources().getString(R.string.sample_text)),
					bundle.getString(Constants.EXTRA_PATH, null),
					PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.Preferences.STORAGE,
																					  true));
			readable.setPosition(Math.max(bundle.getInt(Constants.EXTRA_POSITION), 0));
			readable.setHeader(bundle.getString(Constants.EXTRA_HEADER));
		}
		return readable;
	}

	public static Readable createReadable(Integer intentType, String intentText, String intentPath,
										  Boolean cacheEnabled){
		Readable readable;
		switch (intentType){
			case TYPE_RAW:
				readable = new RawReadable(intentText, false); //currently it's only for test
				break;
			case TYPE_CLIPBOARD:
				readable = new ClipboardReadable();
				break;
			case TYPE_FILE:
				readable = FileStorable.createFileStorable(intentPath);
				break;
			case TYPE_TXT:
				readable = new TxtFileStorable(intentPath);
				break;
			case TYPE_EPUB:
				readable = new EpubFileStorable(intentPath);
				break;
			default:
				String link;
				if (!TextUtils.isEmpty(intentText) &&
						intentText.length() < Constants.NON_LINK_LENGTH &&
						!TextUtils.isEmpty(link = TextParser.findLink(TextParser.compilePattern(), intentText))){
					readable = new NetStorable(link);
				} else {
					readable = new RawReadable(intentText, cacheEnabled); //neutral value, actually
				}
		}
		return readable;
	}

	abstract public void process(Context context);

	public Integer getType(){
		return type;
	}

	public void setType(Integer type){
		this.type = type;
	}

	public Long getSeconds(){
		return seconds;
	}

	public void setSeconds(Long seconds){
		this.seconds = seconds;
	}

	public Boolean getProcessFailed(){
		return processFailed;
	}

	public void setProcessFailed(Boolean processFailed){
		this.processFailed = processFailed;
	}

	public String getText(){
		return text.toString();
	}

	public void setText(String text){
		this.text = new StringBuilder(text);
	}

	public String getHeader(){
		return header;
	}

	public void setHeader(String header){
		this.header = header;
	}

	public Long getDateChanged(){
		return seconds;
	}

	public void setDateChanged(Long seconds){
		this.seconds = seconds;
	}

	public String getPath(){
		return path;
	}

	public void setPath(String path){
		this.path = path;
	}

	public Integer getPosition(){
		return position;
	}

	public void setPosition(Integer position){
		this.position = position;
	}

	public List<String> getWordList(){
		return wordList;
	}

	public void setWordList(List<String> wordList){
		this.wordList = wordList;
	}

	public List<Integer> getDelayList(){
		return delayList;
	}

	public void setDelayList(List<Integer> delayList){
		this.delayList = delayList;
	}

	public List<Integer> getEmphasisList(){
		return emphasisList;
	}

	public void setEmphasisList(List<Integer> emphasisList){
		this.emphasisList = emphasisList;
	}

	public static class Builder implements Callable<Readable> {

		Context context;
		Readable readable;

		public Builder(Context context, Bundle bundle){
			this.context = context;
			this.readable = createReadable(context, bundle);
		}

		@Override
		public Readable call() throws Exception{
			readable.process(context);
			return readable;
		}
	}
}
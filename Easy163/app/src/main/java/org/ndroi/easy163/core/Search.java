package org.ndroi.easy163.core;

import android.util.Log;
import org.ndroi.easy163.providers.Provider;
import org.ndroi.easy163.utils.ConcurrencyTask;
import org.ndroi.easy163.utils.EasyLog;
import org.ndroi.easy163.utils.Keyword;
import org.ndroi.easy163.utils.Song;
import java.util.List;

/* search Song from providers */
public class Search
{
    public static Song search(Keyword targetKeyword)
    {
        List<Provider> providers = Provider.getProviders(targetKeyword);
        Log.d("search", "start to search: " + targetKeyword.toString());
        EasyLog.log("开始全网搜索：" + targetKeyword.toString());
        ConcurrencyTask concurrencyTask = new ConcurrencyTask();
        for (Provider provider : providers)
        {
            concurrencyTask.addTask(new Thread(){
                @Override
                public void run()
                {
                    super.run();
                    provider.collectCandidateKeywords();
                }
            });
        }
        long startTime = System.currentTimeMillis();
        while (true)
        {
            if(concurrencyTask.isAllFinished())
            {
                Log.d("search", "all providers finish collect");
                break;
            }
            long endTime = System.currentTimeMillis();
            if (endTime - startTime > 8 * 1000)
            {
                Log.d("search", "collect candidateKeywords timeout");
                break;
            }
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        Provider bestProvider = Provider.selectCandidateKeywords(providers);
        if (bestProvider != null)
        {
            Log.d("search", "bestProvider " + bestProvider.toString());
            Song song = null;
            try
            {
                song = bestProvider.fetchSelectedSong();
            }catch (Exception e)
            {
                Log.d("search", "fetchSelectedSong failed");
                e.printStackTrace();
            }
            if(song != null)
            {
                Log.d("search", "from provider:\n" + song.toString());
                EasyLog.log("搜索到音源：" +  "[" + bestProvider.getProviderName() + "] " +
                        bestProvider.getSelectedKeyword().toString());
            }else
            {
                Log.d("search", "fetchSelectedSong failed");
                EasyLog.log("未搜索到音源：" + targetKeyword.toString());
            }
            return song;
        }
        EasyLog.log("未搜索到音源：" + targetKeyword.toString());
        return null;
    }
}
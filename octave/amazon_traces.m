clear
close all hidden
clc

directory = "/Users/eugenio/Desktop/fair-tasks/AWS";
doShuffling = true;

map = rs = shuffle = cell (5, 1);
nm = nr = zeros (5, 1);

subdirs = glob ([directory, "/R*"]);
for ii = 1:numel (subdirs)
  dir = subdirs{ii};
  aux = load ([dir, "/numTasks.txt"]);
  nm(ii) = aux(1);
  nr(ii) = aux(2);
  map{ii} = load ([dir, "/Map 1.txt"]);
  rs{ii} = load ([dir, "/rs 2.txt"]);
  shuffle{ii} = load ([dir, "/Shuffle 2.txt"]);
endfor

avgMap = devMap = avgRs = devRs = avgShuffle = zeros (size (map));
scaledMap = scaledRs = cell (size (map));

for ii = 1:numel (map)
  [~, avgMap(ii), devMap(ii)] = zscore (map{ii});
  [~, avgRs(ii), devRs(ii)] = zscore (rs{ii});
  avgShuffle(ii) = mean (shuffle{ii});
endfor

fraction = mean (arrayfun (@(ii) avgShuffle(ii) / avgRs(ii), 1:numel(avgShuffle)));

finalMap = finalRs = cell (size (map));
finalMaxMap = finalMaxRs = finalAvgMap = finalAvgRs = zeros (size (map));

percent = 0.2;

for ii = 1:numel (map)
  finalMap{ii} = map{ii}(abs (map{ii} - avgMap(ii)) < 2 * devMap(ii));
  finalAvgMap(ii) = mean (finalMap{ii});
  finalMaxMap(ii) = max (finalMap{ii});
  
  finalRs{ii} = rs{ii}(abs (rs{ii} - avgRs(ii)) < 2 * devRs(ii));
  finalAvgRs(ii) = mean (finalRs{ii});
  finalMaxRs(ii) = max (finalRs{ii});
endfor

if (doShuffling)
  rand ("seed", 17);
  for ii = 1:numel (finalMap)
    finalMap{ii} = finalMap{ii}(randperm (numel (finalMap{ii})));
    finalRs{ii} = finalRs{ii}(randperm (numel (finalRs{ii})));
  endfor
endif

output_dir = "tracce_amazon_shuffle";

for ii = 1:numel (map)
  class = num2str (ii);
  dir = [output_dir, "/", class];
  mkdir (dir);
  
  indir = [dir, "/2xlarge"];
  mkdir (indir);
  
  aux = round (finalMap{ii});
  save ([indir, "/map.txt"], "aux");
  aux = round (finalRs{ii});
  save ([indir, "/rs.txt"], "aux");
  
  fid = fopen ([indir, "/param.txt"], "w");
  fprintf (fid, "Avg Map: %d\n", round (finalAvgMap(ii)));
  fprintf (fid, "Max Map: %d\n\n", round (finalMaxMap(ii)));
  
  fprintf (fid, "Avg RS: %d\n", round (finalAvgRs(ii)));
  fprintf (fid, "Max RS: %d\n\n", round (finalMaxRs(ii)));
  
  fprintf (fid, "Avg Reduce: %d\n", round ((1 - fraction) * finalAvgRs(ii)));
  fprintf (fid, "Max Reduce: %d\n\n", round ((1 - fraction) * finalMaxRs(ii)));
  
  fprintf (fid, "Avg Shuffle: %d\n", round (fraction * finalAvgRs(ii)));
  fprintf (fid, "Max Shuffle: %d\n\n", round (fraction * finalMaxRs(ii)));
  fclose (fid);
  
  fid = fopen ([indir, "/numTasks.txt"], "w");
  fprintf (fid, "Map: %d\n", nm(ii));
  fprintf (fid, "RS: %d\n", nr(ii));
  fclose (fid);
endfor

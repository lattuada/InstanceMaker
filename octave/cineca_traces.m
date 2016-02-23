clear
close all hidden
clc

directory = "/Users/eugenio/Desktop/fair-tasks/Fair_Scheduler_";

map = rs = shuffle = cell (5, 2);
nm = nr = zeros (5, 2);

for ii = 1:size (map, 1)
  jj = 1;
  for dir = glob ([directory, num2str(ii), "/R*"])'
    dir = dir{1};
    aux = load ([dir, "/numTasks.txt"]);
    nm(ii, jj) = aux(1);
    nr(ii, jj) = aux(2);
    map{ii, jj} = load ([dir, "/Map 1.txt"]);
    rs{ii, jj} = load ([dir, "/rs 2.txt"]);
    shuffle{ii, jj++} = load ([dir, "/Shuffle 2.txt"]);
  endfor
endfor

avgMap = devMap = avgRs = devRs = avgShuffle = zeros (size (map));
scaledMap = scaledRs = cell (size (map));

for ii = 1:size (map, 1)
  for jj = 1:size (map, 2)
    [~, avgMap(ii, jj), devMap(ii, jj)] = zscore (map{ii, jj});
    [~, avgRs(ii, jj), devRs(ii, jj)] = zscore (rs{ii, jj});
    avgShuffle(ii, jj) = mean (shuffle{ii, jj});
  endfor
endfor

fraction = mean (arrayfun (@(ii) avgShuffle(ii) / avgRs(ii), 1:numel(avgShuffle)));

[rows, cols] = size (map);
finalMap = finalRs = cell (rows, cols);
finalMaxMap = finalMaxRs = finalAvgMap = finalAvgRs = zeros (rows, cols);

percent = 0.2;

for ii = 1:size (map, 1)
  for jj = 1:size (map, 2)
    finalMap{ii, jj} = map{ii, jj}(abs (map{ii, jj} - avgMap(ii, jj)) < 2 * devMap(ii, jj));
    finalAvgMap(ii, jj) = mean (finalMap{ii, jj});
    finalMaxMap(ii, jj) = max (finalMap{ii, jj});
    
    finalRs{ii, jj} = rs{ii, jj}(abs (rs{ii, jj} - avgRs(ii, jj)) < 2 * devRs(ii, jj));
    finalAvgRs(ii, jj) = mean (finalRs{ii, jj});
    finalMaxRs(ii, jj) = max (finalRs{ii, jj});
  endfor
endfor

output_dir = "tracce_cineca";

for ii = 1:rows
  for jj = 1:cols
    class = num2str ((ii - 1) * cols + jj);
    dir = [output_dir, "/", class];
    mkdir (dir);
    
    indir = [dir, "/5xlarge"];
    mkdir (indir);
    
    aux = round (finalMap{ii, jj});
    save ([indir, "/map.txt"], "aux");
    aux = round (finalRs{ii, jj});
    save ([indir, "/rs.txt"], "aux");
    
    fid = fopen ([indir, "/param.txt"], "w");
    fprintf (fid, "Avg Map: %d\n", round (finalAvgMap(ii, jj)));
    fprintf (fid, "Max Map: %d\n\n", round (finalMaxMap(ii, jj)));
    
    fprintf (fid, "Avg RS: %d\n", round (finalAvgRs(ii, jj)));
    fprintf (fid, "Max RS: %d\n\n", round (finalMaxRs(ii, jj)));
    
    fprintf (fid, "Avg Reduce: %d\n", round ((1 - fraction) * finalAvgRs(ii, jj)));
    fprintf (fid, "Max Reduce: %d\n\n", round ((1 - fraction) * finalMaxRs(ii, jj)));
    
    fprintf (fid, "Avg Shuffle: %d\n", round (fraction * finalAvgRs(ii, jj)));
    fprintf (fid, "Max Shuffle: %d\n\n", round (fraction * finalMaxRs(ii, jj)));
    fclose (fid);
    
    fid = fopen ([indir, "/numTasks.txt"], "w");
    fprintf (fid, "Map: %d\n", nm(ii, jj));
    fprintf (fid, "RS: %d\n", nr(ii, jj));
    fclose (fid);
  endfor
endfor

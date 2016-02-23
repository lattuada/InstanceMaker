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
    [scaledMap{ii, jj}, avgMap(ii, jj), devMap(ii, jj)] = zscore (map{ii, jj});
    [scaledRs{ii, jj}, avgRs(ii, jj), devRs(ii, jj)] = zscore (rs{ii, jj});
    avgShuffle(ii, jj) = mean (shuffle{ii, jj});
  endfor
endfor

fraction = mean (arrayfun (@(ii) avgShuffle(ii) / avgRs(ii), 1:numel(avgShuffle)));

newAvgMap = [1.2 1.4 1.8 2.0 2.4 2.8 4.2 4.8 5.1 5.4] * 1e4;
newDevMap = [1.7 2.4 1.8 2.0 3.4 4.8 5.2 5.8 6.1 7.4] * 1e3;
newAvgRs = [5.2 5.4 6.8 7.0 8.4 7.8 9.2 9.8 10.1 10.4] * 1e3;
newDevRs = [0.8 0.9 1.2 1.7 2.4 2.4 10.2 10.8 11.1 12.3] * 1e3;

rand ("seed", 17);
newAvgMap = newAvgMap(randperm (10));
newDevMap = newDevMap(randperm (10));
newAvgRs = newAvgRs(randperm (10));
newDevRs = newDevRs(randperm (10));

newAvgMap = reshape (newAvgMap, 5 ,2);
newDevMap = reshape (newDevMap, 5 ,2);
newAvgRs = reshape (newAvgRs, 5 ,2);
newDevRs = reshape (newDevRs, 5 ,2);

[rows, cols] = size (map);
finalMap = finalRs = cell (rows, cols, 3);
finalMaxMap = finalMaxRs = finalAvgMap = finalAvgRs = zeros (rows, cols, 3);

percent = 0.2;

for ii = 1:size (map, 1)
  for jj = 1:size (map, 2)
    for kk = 1:3
      avg = newAvgMap(ii, jj) * (1 + (kk - 2) * percent);
      finalMap{ii, jj, kk} = abs (scaledMap{ii, jj} * newDevMap(ii, jj) + avg);
      tmpAvg = mean (finalMap{ii, jj, kk});
      tmpDev = std (finalMap{ii, jj, kk});
      finalMap{ii, jj, kk} = finalMap{ii, jj, kk}(abs (finalMap{ii, jj, kk} - tmpAvg) < 2 * tmpDev);
      finalAvgMap(ii, jj, kk) = mean (finalMap{ii, jj, kk});
      finalMaxMap(ii, jj, kk) = max (finalMap{ii, jj, kk});
      
      avg = newAvgRs(ii, jj) * (1 + (2 - kk) * fraction * percent);
      finalRs{ii, jj, kk} = abs (scaledRs{ii, jj} * newDevRs(ii, jj) + avg);
      tmpAvg = mean (finalRs{ii, jj, kk});
      tmpDev = std (finalRs{ii, jj, kk});
      finalRs{ii, jj, kk} = finalRs{ii, jj, kk}(abs (finalRs{ii, jj, kk} - tmpAvg) < 2 * tmpDev);
      finalAvgRs(ii, jj, kk) = mean (finalRs{ii, jj, kk});
      finalMaxRs(ii, jj, kk) = max (finalRs{ii, jj, kk});
    endfor
  endfor
endfor

output_dir = "nuove_tracce";
vms = {"medium", "large", "xlarge"};

for ii = 1:rows
  for jj = 1:cols
    class = num2str ((ii - 1) * cols + jj);
    dir = [output_dir, "/", class];
    mkdir (dir);
    
    for kk = 1:3
      indir = [dir, "/", vms{kk}];
      mkdir (indir);
      
      aux = round (finalMap{ii, jj, kk});
      save ([indir, "/map.txt"], "aux");
      aux = round (finalRs{ii, jj, kk});
      save ([indir, "/rs.txt"], "aux");
      
      fid = fopen ([indir, "/param.txt"], "w");
      fprintf (fid, "Avg Map: %d\n", round (finalAvgMap(ii, jj, kk)));
      fprintf (fid, "Max Map: %d\n\n", round (finalMaxMap(ii, jj, kk)));
      
      fprintf (fid, "Avg RS: %d\n", round (finalAvgRs(ii, jj, kk)));
      fprintf (fid, "Max RS: %d\n\n", round (finalMaxRs(ii, jj, kk)));
      
      fprintf (fid, "Avg Reduce: %d\n", round ((1 - fraction) * finalAvgRs(ii, jj, kk)));
      fprintf (fid, "Max Reduce: %d\n\n", round ((1 - fraction) * finalMaxRs(ii, jj, kk)));
      
      fprintf (fid, "Avg Shuffle: %d\n", round (fraction * finalAvgRs(ii, jj, kk)));
      fprintf (fid, "Max Shuffle: %d\n\n", round (fraction * finalMaxRs(ii, jj, kk)));
      fclose (fid);
      
      fid = fopen ([indir, "/numTasks.txt"], "w");
      fprintf (fid, "Map: %d\n", nm(ii, jj));
      fprintf (fid, "RS: %d\n", nr(ii, jj));
      fclose (fid);
    endfor
  endfor
endfor

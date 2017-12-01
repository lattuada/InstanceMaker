## Copyright 2016 Eugenio Gianniti
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.

## -*- texinfo -*- 
## @deftypefn {Function File} {} prepare_traces (@var{directory}, @var{vm}, @var{reorder})
##
## Retrieve task and profile data from @var{directory} and set up traces in the
## working directory, taking out outliers.
## The parameter @var{vm} should be the name of a VM type present in the SPACE4Cloud WS
## database.
## If @var{reorder} is true, then randomly shuffle task durations in the output files.
## Please note that this function works only for simple MapReduce jobs.
##
## @end deftypefn

function prepare_traces (directory, vm, reorder)

if (isdir (directory))
  if (ischar (vm))
    if (islogical (reorder))
      dirs = readdir (directory);
      dirs = sort (dirs(! strncmp (dirs, ".", 1)));

      num = length (dirs);
      map = rs = shuffle = cell (num, 1);
      nm = nr = zeros (num, 1);

      for (ii = 1:num)
        dir = [directory, "/", dirs{ii}];
        aux = load ([dir, "/numTasks.txt"]);
        nm(ii) = aux(1);
        nr(ii) = aux(2);
        map{ii} = load ([dir, "/Map 1.txt"]);
        rs{ii} = load ([dir, "/rs 2.txt"]);
        shuffle{ii} = load ([dir, "/Shuffle 2.txt"]);
      endfor

      sz = size (map);
      avgMap = devMap = avgRs = devRs = avgShuffle = zeros (sz);
      scaledMap = scaledRs = cell (sz);

      for (ii = 1:num)
        [~, avgMap(ii), devMap(ii)] = zscore (map{ii});
        [~, avgRs(ii), devRs(ii)] = zscore (rs{ii});
        avgShuffle(ii) = mean (shuffle{ii});
      endfor

      fraction = mean (avgShuffle ./ avgRs);

      finalMap = finalRs = cell (sz);
      finalMaxMap = finalMaxRs = finalAvgMap = finalAvgRs = zeros (sz);

      for (ii = 1:num)
        finalMap{ii} = map{ii}(abs (map{ii} - avgMap(ii)) < 2 * devMap(ii));
        finalAvgMap(ii) = mean (finalMap{ii});
        finalMaxMap(ii) = max (finalMap{ii});
        
        finalRs{ii} = rs{ii}(abs (rs{ii} - avgRs(ii)) < 2 * devRs(ii));
        finalAvgRs(ii) = mean (finalRs{ii});
        finalMaxRs(ii) = max (finalRs{ii});
      endfor

      if (reorder)
        rand ("seed", 17);
        for (ii = 1:num)
          finalMap{ii} = finalMap{ii}(randperm (numel (finalMap{ii})));
          finalRs{ii} = finalRs{ii}(randperm (numel (finalRs{ii})));
        endfor
      endif

      output_dir = pwd;
      for (ii = 1:num)
        class = dirs{ii};
        dir = [output_dir, "/", class];
        mkdir (dir);
        
        indir = [dir, "/", vm];
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
    else
      error ("REORDER is not logical");
    endif
  else
    error ("VM is not a string");
  endif
else
  error ("DIRECTORY is not a directory");
endif

endfunction

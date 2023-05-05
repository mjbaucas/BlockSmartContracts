import matplotlib.pyplot as plt 
import numpy as np

smart_contract_conf = [46.59, 46.99, 49.85, 63.55, 188.73]
list_conf = [89.53, 87.34, 86.39, 119.62, 321.10]
control_conf = [70.32, 68.54, 70.6, 104.22, 302.69]

length_tick = np.arange(5)
length_label = [100, 1000, 10000, 100000, 1000000]

plt.figure(0)
plt.plot(length_tick, smart_contract_conf, marker='o', label="Configuration 1")
plt.plot(length_tick, list_conf, marker='o', label="Configuration 2")
plt.plot(length_tick, control_conf, marker='o', label="Configuration 3")
plt.legend()
plt.xticks(length_tick, length_label)
plt.grid(linestyle = '--', linewidth = 0.5)
plt.ylabel("Average Latency (ms)")
plt.xlabel("Number of Database/Blockchain Entries")
plt.savefig('latency.png')
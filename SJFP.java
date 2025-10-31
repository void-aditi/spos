import java.util.Scanner;

public class SJFP
{
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
		
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int[] at = new int[n];
        int[] bt = new int[n];
        int[] rt = new int[n];
        int[] ct = new int[n];
        int[] wt = new int[n];
        int[] tat = new int[n];
        boolean[] completed = new boolean[n];

        for (int i = 0; i < n; i++)
        {
            System.out.print("\nEnter Arrival Time for Process " + (i + 1) + ": ");
            at[i] = sc.nextInt();
            System.out.print("Enter Burst Time for Process " + (i + 1) + ": ");
            bt[i] = sc.nextInt();
            rt[i] = bt[i];
        }

        int pt = 0, completedCount = 0;

        while (completedCount < n)
        {
            int idx = -1;
            int minRt = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++)
            {
                if (at[i] <= pt && !completed[i] && rt[i] < minRt && rt[i] > 0)
                {
                    minRt = rt[i];
                    idx = i;
                }
            }

            if (idx != -1)
            {
                rt[idx]--;
                pt++;

                if (rt[idx] == 0)
                {
                    completed[idx] = true;
                    completedCount++;
                    ct[idx] = pt;
                    tat[idx] = ct[idx] - at[idx];
                    wt[idx] = tat[idx] - bt[idx];
                }
            }
            else
            {
                pt++;
            }
        }

        System.out.println("\nProcess | Arrival Time | Burst Time | Waiting Time | Turnaround Time");
        System.out.println("---------------------------------------------------------------");
        for (int i = 0; i < n; i++)
        {
            System.out.println("   P" + (i + 1) + " \t      " + at[i] + " \t      " + bt[i] + " \t     " + wt[i] + "  \t  " + tat[i]);
        }

        int totalWT = 0, totalTAT = 0;
        for (int i = 0; i < n; i++)
        {
            totalWT += wt[i];
            totalTAT += tat[i];
        }

        float avgWT = (float) totalWT / n;
        float avgTAT = (float) totalTAT / n;

        System.out.println("\nAverage Waiting Time: " + avgWT);
        System.out.println("Average Turnaround Time: " + avgTAT);
    }
}

import java.util.Scanner;

public class MatrixProduct {
    private static void OnMult(int m_ar, int m_br)
    {
        long Time1, Time2;

        double temp;
        int i, j, k;

        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        for(i=0; i<m_ar; i++)
            for(j=0; j<m_ar; j++)
                pha[i*m_ar + j] = (double)1.0;



        for(i=0; i<m_br; i++)
            for(j=0; j<m_br; j++)
                phb[i*m_br + j] = (double)(i+1);

        Time1 = System.currentTimeMillis();

        for(i=0; i<m_ar; i++)
        {	for( j=0; j<m_br; j++)
            {	temp = 0;
                for( k=0; k<m_ar; k++)
                {
                    temp += pha[i*m_ar+k] * phb[k*m_br+j];
                }
                phc[i*m_ar+j]=temp;
            }
        }

        Time2 = System.currentTimeMillis();
        System.out.printf("Time: %3.3f seconds\n", (double)(Time2 - Time1) / 1000);

        System.out.println("Result matrix: ");

        for(i=0; i<1; i++)
        {	for(j=0; j<Math.min(10,m_br); j++)
            System.out.print(phc[j] + " ");
        }

        System.out.println();
    }

    private static void OnMultLine(int m_ar, int m_br)
    {
        long Time1, Time2;

        double temp;
        int i, j, k;

        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        for(i=0; i<m_ar; i++)
            for(j=0; j<m_ar; j++)
                pha[i*m_ar + j] = (double)1.0;

        for(i=0; i<m_br; i++)
            for(j=0; j<m_br; j++)
                phb[i*m_br + j] = (double)(i+1);

        for(i=0; i<m_br; i++)
            for(j=0; j<m_br; j++)
                phc[i*m_br + j] = (double)0.0;

        Time1 = System.currentTimeMillis();

        for (i = 0; i < m_ar; i++) {
            for (k = 0; k < m_ar; k++) {
                for (j = 0; j < m_ar; j++) {
                    phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_ar+j];
                }
            }
        }

        Time2 = System.currentTimeMillis();
        System.out.printf("Time: %3.3f seconds\n", (double)(Time2 - Time1) / 1000);

        System.out.println("Result matrix: ");

        for(i=0; i<1; i++)
        {	for(j=0; j<Math.min(10,m_br); j++)
            System.out.print(phc[j] + " ");
        }

        System.out.println();


    }

    public static void main(String args[]) {
        char c;
        int lin, col, blockSize;
        int op;

        op=1;
        Scanner in = new Scanner(System.in);
        do {
            System.out.println("\n1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.println("Selection?: ");

            op = Integer.parseInt(in.nextLine());

            if (op == 0)
                break;

            System.out.println("Dimensions: lins=cols ? ");

            lin = Integer.parseInt(in.nextLine());
            col = lin;

            switch (op){
                case 1:
                    OnMult(lin, col);
                    break;
                case 2:
                    OnMultLine(lin, col);
                    break;
            }

        } while (op != 0);
    }
}

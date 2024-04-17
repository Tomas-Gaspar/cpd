#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t

void OnMult(int m_ar, int m_br) 
{
    
    char st[100];
    double temp;
    int i, j, k;

    double *pha, *phb, *phc;
    

        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;



    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);


    for(i=0; i<m_ar; i++)
    {    for( j=0; j<m_br; j++)
        {    temp = 0;
            for( k=0; k<m_ar; k++)
            {    
                temp += pha[i*m_ar+k] * phb[k*m_br+j];
            }
            phc[i*m_ar+j]=temp;
        }
    }

    cout << st;

    // display 10 elements of the result matrix tto verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {    for(j=0; j<min(10,m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
    
}

void OnMultLine(int m_ar, int m_br)
{
    
    char st[100];
    double temp;
    int i, j, k;

    double *pha, *phb, *phc;
    

        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;


    for (i = 0; i < m_ar; i++) {
        for (k = 0; k < m_ar; k++) {
            for (j = 0; j < m_ar; j++) {
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_ar+j];
            }
        }
    }
    cout << st;

    // display 10 elements of the result matrix tto verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {    for(j=0; j<min(10,m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
    
}

int min(int a, int b) {
    return a < b ? a : b;
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
    
    char st[100];
    double temp;
    int i, j, k, ii, jj, kk;

    double *pha, *phb, *phc;
    
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

    
    for (ii = 0; ii < m_ar; ii+=bkSize) {
		for (kk = 0; kk < m_ar; kk+=bkSize) {
        	for (jj = 0; jj < m_br; jj+=bkSize) {
                int block_size_i = min(bkSize, m_ar - ii);
                int block_size_k = min(bkSize, m_ar - kk);
                int block_size_j = min(bkSize, m_br - jj);

                for (i = ii; i < ii + block_size_i; i++) {
					for (k = kk; k < kk + block_size_k; k++) {
	                    for (j = jj; j < jj + block_size_j; j++) {
                            phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_ar+j];
                        }
                    }
                }
            }
        }
    }
	cout << st;

    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {    for(j=0; j<min(10,m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);   
    
}

void OnMultLineParallel1(int m_ar, int m_br)
{
    char st[100];
    double temp;

    double *pha, *phb, *phc;
    

        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(int i=0; i<m_ar; i++)
        for(int j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(int i=0; i<m_br; i++)
        for(int j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);

    for(int i=0; i<m_br; i++)
        for(int j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;


    #pragma omp parallel for
    for (int i = 0; i < m_ar; i++) {
        for (int k = 0; k < m_ar; k++) {
            for (int j = 0; j < m_ar; j++) {
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_ar+j];
            }
        }
    }

    cout << st;

    // display 10 elements of the result matrix tto verify correctness
    cout << "Result matrix: " << endl;
    for(int i=0; i<1; i++)
    {    for(int j=0; j<min(10,m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
    
}

void OnMultLineParallel2(int m_ar, int m_br)
{
    
    char st[100];
    double temp;

    double *pha, *phb, *phc;
    

        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(int i=0; i<m_ar; i++)
        for(int j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(int i=0; i<m_br; i++)
        for(int j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);

    for(int i=0; i<m_br; i++)
        for(int j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;


    #pragma omp parallel
    for (int i = 0; i < m_ar; i++) {
        for (int k = 0; k < m_ar; k++) {
            #pragma omp for
            for (int j = 0; j < m_ar; j++) {
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_ar+j];
            }
        }
    }


    cout << st;

    // display 10 elements of the result matrix tto verify correctness
    cout << "Result matrix: " << endl;
    for(int i=0; i<1; i++)
    {    for(int j=0; j<min(10,m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
    
}




int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	
	
	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
        cout << "4. Line Multiplication Parallel 1" << endl;
        cout << "5. Line Multiplication Parallel 2" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
   		cin >> lin;
   		col = lin;


		switch (op){
			case 1: 
				OnMult(lin, col);
				break;
			case 2:
				OnMultLine(lin, col);  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				OnMultBlock(lin, col, blockSize); 
                break; 
            case 4:
                OnMultLineParallel1(lin, col);
				break;
            case 5:
                OnMultLineParallel2(lin, col);
                break;

		}

	}while (op != 0);
}
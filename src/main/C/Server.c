/*
 * This code is based on https://github.com/lotabout/buddy-system.git *
 * The buddy system algorithm from the user lotabout from GitHub      *
 */ 
#include "buddy.h"
#include <unistd.h>
#include <time.h>
#include <math.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/socket.h>
#include <arpa/inet.h>

#define PORT 4567
#define ALLOCATE 0
#define FREE 1
#define THREADS	5
#define MEMORY_MAXIMUM_SIZE	1024
#define MEMORY_MINIMUN_SIZE	32
#define MEMORY_MINIMUN_ALLOCATION 2
#define LAMBDA_ALLOC 0.07
#define LAMBDA_FREE 0.2
#define	BUFFER_SIZE	50

double getTimeExponential(int);
void initClient(struct buddy*, int);
void closeProgram(void);
void* responseClient(void*);
void* Thread(void*);
void buffer_fill(int, int, int);
int randomMemory(void);
bool twoPower(int);
int nearPowerOfTwo(int);

struct Process {
	pthread_t id;
	int PID;
	int socket;
	struct buddy* buddy;
};


int memorySize = 5;
sem_t mutex;
struct Process process[THREADS];
static char buffer[BUFFER_SIZE];


int main(void) {
	int socket_descriptor, client_descriptor, lenAddr;
	struct sockaddr_in server, client;
	pthread_t client_connection;
	srand(time(NULL));

	socket_descriptor = socket(AF_INET, SOCK_STREAM, 0);
	if (socket_descriptor == -1) {
		printf("Imposible iniciar el socket...\n");
	}
	printf("Socket creado con exito...\n");

	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons(PORT);

	if (bind(socket_descriptor, (struct sockaddr*)&server, sizeof(server)) < 0) {
		printf("Imposible publicar el servicio en el puerto %d...\n", PORT);
		return -1;
	}
	printf("Socket preparado en el puerto %d...\n", PORT);
	listen(socket_descriptor, 3);

	printf("Esperando al cliente...\n");
	lenAddr = sizeof(struct sockaddr_in);

	while (client_descriptor = accept(socket_descriptor, (struct sockaddr*)&client, (socklen_t*)&lenAddr)) {
		printf("Cliente conectado...\nEmpezando algoritmo...\n");

		if(pthread_create(&client_connection, NULL, responseClient, (void*) &client_descriptor) != 0) {		
			perror("Problemas al comunicarse con el cliente...\nError faltal...\n");
			return -1;
        }
		pthread_join(client_connection, NULL);
		break;
	}
	
	pthread_cancel(client_connection);
	close(socket_descriptor);
	puts("Cerrando servidor...\n");
	return 0;
}

void *responseClient(void *client_descriptor) {
	int socket = *(int*)client_descriptor;
	int read_size, send_size, ind, initial = 1;
	char *message, client_message[BUFFER_SIZE];
	pthread_t controls, client = pthread_self();
	struct buddy* buddy = NULL;
	
	do {
		memset(client_message, 0, BUFFER_SIZE);
		read_size = recv(socket, client_message, BUFFER_SIZE * sizeof(char), 0);

		if (initial) {
			if (read_size > 0) {
				client_message[read_size] = '\0';
				memorySize = atoi(client_message);
				printf("Tamaño elegido por el cliente: %d\n", memorySize);

				if (twoPower(memorySize) && (memorySize >= MEMORY_MINIMUN_SIZE && memorySize <= MEMORY_MAXIMUM_SIZE)) {
					sleep(3);
					buddy = buddy_new(memorySize);
					initClient(buddy, socket);
					initial = 0;
				} else {
					printf("Valor de memoria incorrecto...\n");
					send_size = write(socket, message, strlen(message));
					initial++;
				}
			}
		} else {
			if (read_size > 0) {
				printf("Mensaje del cliente: %s\n", client_message);
			}
		}

		if (read_size > 0) {
			continue;
		} else if (read_size == 0) {
			closeProgram();
			printf("El cliente se ha desconectado...\n");
			fflush(stdout);
		} else {
			perror("Error al recibir mensajes del cliente...\n");
			closeProgram();
			printf("El cliente se ha desconectado...\n");
			fflush(stdout);
		}
	} while(read_size > 0);
	close(socket);
}

void initClient(struct buddy* buddy, int socket) {
	sem_init(&mutex, 0, 1);
		
	char* tempBuffer;
 	tempBuffer = malloc(buddy->size * 10);
	int tam = 0;

	for (int i = 0; i < buddy->size; i++) {
		tam += sprintf(&tempBuffer[tam], "%d:%ld,", i, buddy->longest[i]);
	}
	sprintf(&tempBuffer[tam], "\n");
	printf("\n%s\n", tempBuffer);
	write(socket, tempBuffer, strlen(tempBuffer));

	for (int i = 0; i < THREADS; i++) {
		pthread_create(&process[i].id, NULL, &Thread, &process[i]);
		process[i].PID = i;
		process[i].socket = socket;
		process[i].buddy = buddy;
	}
}

void closeProgram() {
	for(int i = 0; i < memorySize; i++) {
		pthread_cancel(process[i].id);
	}
}

void* Thread(void* args) {
	struct Process* proccess = (struct Process*) args;
	int size, position, count = 0;

	while (1) {
		sleep(getTimeExponential(ALLOCATE));
		size = nearPowerOfTwo(randomMemory());
		int index_mod_alloc, index_mod_free;
		do {
			sem_wait(&mutex);
			position = buddy_alloc(proccess->buddy, size, &index_mod_alloc);
			sem_post(&mutex);
			count++;
		} while (position == -1 && count != 10);	
		
		if (position != -1) {
			size = nearPowerOfTwo(size);

			sem_wait(&mutex);
			buffer_fill(ALLOCATE, index_mod_alloc, size);
			write(process[0].socket, buffer, strlen(buffer));
			sleep(1);
			sem_post(&mutex);

			sleep(getTimeExponential(FREE));
			buddy_free(process->buddy, position, &index_mod_free);

			sem_wait(&mutex);
			buffer_fill(FREE, index_mod_free, size);
			write(process[0].socket, buffer, strlen(buffer));
			sleep(1);
			sem_post(&mutex);
		}
		count = 0;
	}
}

void buffer_fill(int command, int position, int size) {
	char position_temp[10], size_temp[10], command_temp[10];
	memset(buffer, 0, BUFFER_SIZE);
	sprintf(position_temp, "%d", position);
	sprintf(size_temp, "%d", size);
	if (command == ALLOCATE) {
		strcat(command_temp, "ALLOC");
	}
	if (command == FREE) {
		strcat(command_temp, "FREE");
	}
	strcat(buffer, command_temp);
	strcat(buffer, "@");
	strcat(buffer, position_temp);
	strcat(buffer, ":");
	strcat(buffer, size_temp);
	strcat(buffer, "\n");
}

int randomMemory() {
	int random;
	int max_number = memorySize, minimum_number = 1; 
	random = rand() % (max_number + 1 - minimum_number) + minimum_number;
	return random;
}

double getTimeExponential(int operation){
    double probability = ((double)rand())/(double)RAND_MAX;
    probability = (probability >= 1)?0.999:probability;
    probability = (probability <= 0)?0.001:probability;
	double time = 0;
	if (operation == ALLOCATE) {
		time = -1.0 * (log(1 - probability)/(double)LAMBDA_ALLOC);
	} else if (operation == FREE) {
		time = -1.0 * (log(1 - probability)/(double)LAMBDA_FREE);
	}
    return time;
}

bool twoPower (int number) {
  return number && (!(number&(number-1)));
}

int nearPowerOfTwo(int number) {
	int x = MEMORY_MINIMUN_ALLOCATION;
	for (int i = 1; i < 10; i++) {
		if (number <= x) {
			return x;
		} else {
			x *= 2;
		}	
	}
	return 0;
}
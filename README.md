# Praćtica 1 - Control de calidad de una aplicación web 

**Grupo 2**

## Miembros del Equipo
| Nombre y Apellidos | Correo URJC | Usuario GitHub |
|:--- |:--- |:--- |
| Marcos García García | m.garciaga.2022@alumnos.urjc.es | marcosgrc |
| Adrián Muñoz Serrano | a.munozse.2022@alumnos.urjc.es | adri04ms |
| Jorge Padilla Rodríguez | j.padilla.2021@alumnos.urjc.es | Jorge-PR |
| Naroa Martín Simón | n.martins.2022@alumnos.urjc.es | NaroaMS04 |
| Alberto Mayoral Gómez | a.mayoral.2022@alumnos.urjc.es | Albermg27 |
| Icíar Moreno López | i.morenolo.2022@alumnos.urjc.es | IciarML |
| Laura Pineda Ballesteros | l.pineda.2022@alumnos.urjc.es | lauraxpb |

---

### **Participación de Miembros en la Práctica 1**

#### **Alumno 1 - Marcos García García**

Para esta práctica he realizado las siguientes tareas: organizar la plantilla inicial del documento ANALISIS_CALIDAD.md, añadir el code smell 1 (Literales duplicados) detectado por SonarCloud y añadir dos code smells detectados manualmente, el 1 (Duplicación de código en métodos deposit) y el 4 (Método demasiado largo).

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Plantilla de la memoria .md](https://github.com/Albermg27/cs-2026-grupo-2/commit/77c3c9600e47dc3e24a7eba3e93b1fbdb354e9d3)  |
|2| [Bad Smell: Duplicación de código en métodos deposit](https://github.com/Albermg27/cs-2026-grupo-2/commit/0735e27565d2932f82a75d56360c51f2d46cfdf6)  |
|3| [Bad Smells: Literales duplicados y Método demasiado largo](https://github.com/Albermg27/cs-2026-grupo-2/commit/ea6557df578fb3ab0acf6d9aa2bb0c1ff5ac259b)  |


---

#### **Alumno 2 - Adrián Muñoz Serrano**

Para esta práctica he realizado las siguientes tareas: he identificado manualmente dos code smells en el proyecto; Data Clumps y Magic Numbers, ambos localizados en la clase AccountService.java. Además, he añadido el code smell 2 (Variable local no utilizada) y el code smell 3 (Comparación de Strings con operador de identidad) detectados por SonarCloud.

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Bad Smells: Data Clumps y Magic Numbers](https://github.com/Albermg27/cs-2026-grupo-2/commit/2008713329e8d1ad508ef72a494dd66ae1d104eb)  |
|2| [Bad Smells SonarCloud: Variable local no utilizada y Comparación de Strings con operador de identidad](https://github.com/Albermg27/cs-2026-grupo-2/commit/29af02cd1448e647b9d99a9d1e8028cc15ce60b2)  |

---

#### **Alumno 3 - Jorge Padilla Rodríguez**

Durante esta práctica he realizado la detección manual de los siguientes code smells: bad smell 11, referente a la existencia de código muerto dentro del método deposit; bad smell 12, que describe código duplicado o redundante dentro del método deposit; bad smell 13, que muestra cómo aparece código duplicado (la validación de una variable) a lo largo de los métodos deposit, withdraw y transfer; bad smell 14, donde vemos que el método rm tiene un nombre poco comunicativo.

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Bad smell 11: código muerto dentro del método deposit](https://github.com/Albermg27/cs-2026-grupo-2/commit/bb9f0a1d3d3ad7f0617ce1bad77b251c15ba8c37)  |
|2| [Bad smells 12 y 13: código duplicado en deposit y en varios métodos](https://github.com/Albermg27/cs-2026-grupo-2/commit/e8e92c37d5d9b2904fbcb7993ad92e329bebb905)  |
|3| [Bad smell 14: nombre de método poco comunicativo](https://github.com/Albermg27/cs-2026-grupo-2/commit/7807b3b361d84d4ff161600c8df9f82b68016a0f)  |

---

#### **Alumno 4 - Naroa Martín Simón**

En esta práctica me he encargado de revisar a mano la clase AccountService.java para encontrar fallos de diseño que SonarCloud no detecta. He analizado dos problemas: Primitive Obsession, que trata sobre cómo el uso excesivo de tipos básicos (como double o String) para el dinero o las cuentas puede causar errores; y Feature Envy, donde he explicado que el servicio está "cotilleando" demasiado el saldo de las cuentas en lugar de dejar que la propia clase Account gestione su lógica interna.

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Bad smells 8 and 9 added: Primitive Obsession y Feature Envy](https://github.com/Albermg27/cs-2026-grupo-2/commit/caa1cef5baf1f8c89de75bc97b75bcaefff4818a)  |

---

#### **Alumno 5 - Alberto Mayoral Gómez**

En esta práctica he realizado una revisión manual del código para identificar problemas de calidad. Como resultado, detecté dos code smells: Mysterious Names / Non-Descriptive Names, relacionado con el uso de nombres poco descriptivos que dificultan la comprensión del código, y Large Class, que indica que una clase tiene demasiadas responsabilidades, afectando a la mantenibilidad y organización del sistema.

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Bad Smell 2: Mysterious Names / Non-Descriptive Names](https://github.com/Albermg27/cs-2026-grupo-2/commit/2738af3575546148ee3ccedcae3b236e8c44a39c)  |
|2| [Bad Smell 3: Large Class](https://github.com/Albermg27/cs-2026-grupo-2/commit/0be9ed6c9634d7111eb1c08acc194acd8dd18ee9)  |

---

#### **Alumno 6 - Icíar Moreno López**

En esta práctica me he encargado de combinar el análisis automático de SonarCloud con una inspección manual de la clase AccountService.java. Mi enfoque se ha centrado en detectar problemas de acoplamiento y falta de polimorfismo que degradan la mantenibilidad del sistema. He analizado en profundidad dos problemas críticos: el uso de Switch Statements en el sistema de avisos, que obligaría a modificar el servicio cada vez que se añada un canal de comunicación, y la Intimidad Inapropiada en la comparación de cuentas, donde el servicio manipula datos internos de las entidades rompiendo el encapsulamiento.

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Bad Smell - Switch Statement](https://github.com/Albermg27/cs-2026-grupo-2/commit/9f86495ce5937a22a79f828cc51459325cf4d39f)  |
|2| [Bad Smell - Inappropriate Intimacy](https://github.com/Albermg27/cs-2026-grupo-2/commit/beb050539ddb2f481fd6e17c7466f96c7d41ae23)  |

---

#### **Alumno 7 - Laura Pineda Ballesteros**

En esta práctica he añadido dos nuevos bad smells en la memoria de análisis: **Generic Exception Catching** (captura genérica de excepciones en controladores) y **Temporary Stub / Incomplete Business Logic** (algoritmo de aprobación de préstamos con retorno fijo de aprobado).

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Bad smells 15 y 16 añadidos en ANALISIS_CALIDAD.md](PENDIENTE_ENLACE_COMMIT)  |

---

### **Participación de Miembros en la Práctica 3**

#### **Alumno 1 - Marcos García García**

Para esta práctica he realizado las siguientes tareas: crear la estructura inicial de AccountServiceTest, los tests del método deposit y los tests para el método createAccount. También he refactorizado los bad smells: literales duplicados, código duplicado (deposit) y método grande (transfer), y añadí la explicación de la refactorización en el fichero ANALISIS_CALIDAD. Finalmente, he creado el test E2E que prueba la transferencia entre cuentas propias, además de crear la estructura inicial de la clase TransferE2ETest.

| Nº    | Commits      |
|:------------: |:------------:|
|1| [Estructura inicial de AccountServiceTest y tests del método deposit](https://github.com/Albermg27/cs-2026-grupo-2/commit/74b2d08d7345186a7dd7153e3d390a2740fb2df4)  |
|2| [Tests para el método createAccount y tests restantes de deposit](https://github.com/Albermg27/cs-2026-grupo-2/commit/b428e721387b0c0f9240f250ff82b59e4cbefd3e)  |
|3| [Refactorizar bad smells: literales duplicados, código duplicado (deposit) y método grande (transfer)](https://github.com/Albermg27/cs-2026-grupo-2/commit/d1a2f1e637796a3de2a0cecb03de51e421af9c94)  |
|4| [Test E2E: Transferencia entre cuentas propias y estructura inicial TransferE2ETest](https://github.com/Albermg27/cs-2026-grupo-2/commit/9d28324185aa76ad39c33fb152a333c150da58c0)  |


---
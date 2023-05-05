-- CreateTable
CREATE TABLE "users" (
    "id" TEXT NOT NULL PRIMARY KEY,
    "version" TEXT NOT NULL DEFAULT '',
    "online" INTEGER NOT NULL DEFAULT 0,
    "lastOnline" TEXT NOT NULL DEFAULT '',
    "firstOnline" TEXT NOT NULL DEFAULT ''
);

-- CreateTable
CREATE TABLE "version" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL DEFAULT '',
    "description" TEXT NOT NULL DEFAULT ''
);

-- CreateTable
CREATE TABLE "action" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "action" TEXT NOT NULL DEFAULT '',
    "content" TEXT NOT NULL DEFAULT '',
    "time" TEXT NOT NULL DEFAULT ''
);

-- CreateTable
CREATE TABLE "image" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "path" TEXT NOT NULL,
    "hash" TEXT NOT NULL,
    "recommendScore" INTEGER NOT NULL,
    "type" INTEGER NOT NULL
);

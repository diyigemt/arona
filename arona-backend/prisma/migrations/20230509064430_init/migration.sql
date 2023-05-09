/*
  Warnings:

  - You are about to drop the column `hash` on the `image` table. All the data in the column will be lost.
  - You are about to drop the column `path` on the `image` table. All the data in the column will be lost.
  - Added the required column `file` to the `image` table without a default value. This is not possible if the table is not empty.

*/
-- CreateTable
CREATE TABLE "file" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "path" TEXT NOT NULL,
    "hash" TEXT NOT NULL,
    "type" INTEGER NOT NULL
);

-- RedefineTables
PRAGMA foreign_keys=OFF;
CREATE TABLE "new_image" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "file" INTEGER NOT NULL,
    "recommendScore" INTEGER NOT NULL,
    "type" INTEGER NOT NULL
);
INSERT INTO "new_image" ("id", "name", "recommendScore", "type") SELECT "id", "name", "recommendScore", "type" FROM "image";
DROP TABLE "image";
ALTER TABLE "new_image" RENAME TO "image";
CREATE UNIQUE INDEX "image_name_key" ON "image"("name");
PRAGMA foreign_key_check;
PRAGMA foreign_keys=ON;

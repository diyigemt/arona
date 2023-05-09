declare module "process" {
  global {
    namespace NodeJS {
      interface ProcessEnv {
        ADMIN_SECRET: string;
      }
    }
  }
}

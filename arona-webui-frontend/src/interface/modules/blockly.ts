export interface BlocklyProject {
  name: string;
  uuid: string | null;
  blocklyProject: string | BlocklyProjectWorkspace;
  userData: string;
}

export interface BlocklyProjectWorkspace {
  [key: string]: string;
}

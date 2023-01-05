export interface BlocklyProject {
  name: string;
  uuid: string | null;
  blocklyProject: string | BlocklyProjectWorkspace;
}

export interface BlocklyProjectWorkspace {
  [key: string]: string;
}

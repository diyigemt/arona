import mitt from "mitt";

type Events = {
  "contact-update": {
    uuid: string;
    value: number;
  };
};

const mainEmitter = mitt<Events>();

export default mainEmitter;

import { ISOtoString } from '@/services/ConvertDateService';

export default class Enrollment {
  id: number | null = null;
  volunteerName!: string;
  volunteerId!: number;
  motivation!: string;
  participating!: boolean;
  enrollmentDateTime!: string;

  constructor(jsonObj?: Enrollment) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.volunteerName = jsonObj.volunteerName;
      this.volunteerId = jsonObj.volunteerId;
      this.motivation = jsonObj.motivation;
      this.participating = jsonObj.participating;
      this.enrollmentDateTime = ISOtoString(jsonObj.enrollmentDateTime);
    }
  }
}
